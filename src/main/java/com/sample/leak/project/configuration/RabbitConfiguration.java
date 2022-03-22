package com.sample.leak.project.configuration;

import com.sample.leak.project.dto.DataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.integration.xml.transformer.MarshallingTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.parsers.ParserConfigurationException;

@Configuration
public class RabbitConfiguration {

    private static Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);

    @Value("${amqpExchangeTarget}")
    private String amqpExchangeTarget;

    @Value("${amqpRoutingKey}")
    private String amqpRoutingKey;

    @Value("${amqpBrokerUri}")
    private String amqpBrokerUri;

    @Bean
    public IntegrationFlowAdapter flow(MessageChannel returnedMessagesChannel,
                                       ConnectionFactory connectionFactory) {
        return new IntegrationFlowAdapter() {
            @Override
            protected IntegrationFlowDefinition<?> buildFlow() {
                return IntegrationFlows.from(inputMessageChannel())
                        .transform(messageRequestsTransformer())
                        .transform(new ObjectToStringTransformer())
                        .log(LoggingHandler.Level.INFO, "amqpOutboundConnectorLogging",
                                "headers.id + ': outboundAMQPPayload=' + payload")
                        .handle(Amqp.outboundAdapter(amqpTemplate(connectionFactory))
                                .confirmCorrelationExpression("payload")
                                .returnChannel(returnedMessagesChannel)
                                .exchangeName(amqpExchangeTarget)
                                .routingKey(amqpRoutingKey)
                                .confirmTimeout(1000)
                                .defaultDeliveryMode(MessageDeliveryMode.PERSISTENT)
                                .headersMappedLast(true)
                        );
            }
        };
    }

    protected RabbitTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(new ContentTypeDelegatingMessageConverter());
        // Always participate in the incoming transaction....
        rabbitTemplate.setChannelTransacted(true);
        // all messages should be routable, if not return message back.
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean(name="amqpConnectionFactory")
    public ConnectionFactory connectionFactory() {

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUri(amqpBrokerUri);
        connectionFactory.setChannelCacheSize(25);
        connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        connectionFactory.setRequestedHeartBeat(60);

        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }

    private MarshallingTransformer messageRequestsTransformer() {
        MarshallingTransformer marshallingTransformer = null;
        try {
            marshallingTransformer = new MarshallingTransformer(messageRequestsMarshaller());
            marshallingTransformer.setResultType("StringResult");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return marshallingTransformer;
    }

    private Jaxb2Marshaller messageRequestsMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.sample.leak.project.dto");
        return marshaller;
    }

    @Bean(name="rabbitAdmin")
    public RabbitAdmin rabbitAdmin(@Qualifier("amqpConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean(name="returnedMessagesChannel")
    public MessageChannel returnedMessagesChannel() {
        return MessageChannels.direct("returnedMessagesChannel")
                .get();
    }

    @ServiceActivator(inputChannel = "returnedMessagesChannel")
    public void processReturnedRequests(Message<?> returnedMessage) {

        logger.error("AMQP broker wasn't able to route message. ");
    }

    @Bean
    public MessageChannel inputMessageChannel() {
        return MessageChannels.direct("inputMessageChannel")
                .datatype(DataDTO.class)
                .get();
    }

    @MessagingGateway
    public interface IOutgoingGateway {
        /**
         * Sending response about saving of incoming payment creation.
         */
        @Gateway(requestChannel = "inputMessageChannel",
                headers = {@GatewayHeader(name="beanName", value="infoAMQPConnector")})
        void sendInfoNotification(DataDTO message);
    }
}
