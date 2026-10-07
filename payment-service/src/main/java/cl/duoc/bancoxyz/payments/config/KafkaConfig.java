package cl.duoc.bancoxyz.payments.config;
import org.apache.kafka.clients.admin.NewTopic; import org.springframework.context.annotation.*; import org.springframework.kafka.config.TopicBuilder;
@Configuration public class KafkaConfig { @Bean NewTopic paymentsTopic(){return TopicBuilder.name("pagos.realizados").partitions(3).replicas(1).build();}@Bean NewTopic alertsTopic(){return TopicBuilder.name("alertas.seguridad").partitions(1).replicas(1).build();} }
