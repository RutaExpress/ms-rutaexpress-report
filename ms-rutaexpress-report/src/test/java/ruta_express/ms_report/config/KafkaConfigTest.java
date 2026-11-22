package ruta_express.ms_report.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// test unitario de la configuración de Kafka: a qué tópico va un mensaje que falló después de los reintentos
class KafkaConfigTest {

    // test 1 (contrato con infra): el mensaje fallido va a report.DLT, en la misma partición
    @Test
    @DisplayName("Mensaje fallido: va a report.DLT en la misma partición")
    void failedRecordGoesToReportDlt() {
        // arrange (preparar): un mensaje de shipments.events en la partición 2
        ConsumerRecord<String, String> record = new ConsumerRecord<>("shipments.events", 2, 10L, "42", "{}");

        // act (actuar)
        TopicPartition destino = KafkaConfig.deadLetterDestination(record, new RuntimeException("falló"));

        // assert (revisar): report.DLT existe en el clúster (crear-topicos.sh), shipments.events.DLT no
        assertEquals("report.DLT", destino.topic());
        assertEquals(2, destino.partition());
    }
}
