package ruta_express.ms_report.config;

// nombres de Kafka centralizados (los mismos que crea rutaexpress-infra/kafka/crear-topicos.sh)
public final class KafkaTopics {

    // tópico que lee report (lo publica shipments; key = id del envío)
    public static final String SHIPMENTS_EVENTS = "shipments.events";

    // tópico de mensajes fallidos de report (un DLT por consumidor, caso sección 9)
    public static final String REPORT_DLT = "report.DLT";

    // constructor privado: solo constantes
    private KafkaTopics() {}
}
