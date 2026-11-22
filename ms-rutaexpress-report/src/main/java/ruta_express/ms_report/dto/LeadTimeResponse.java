package ruta_express.ms_report.dto;

// lead time (tiempo de entrega): cuántos envíos se entregaron en el rango y en cuántos minutos, en promedio,
// pasaron de CREADO a ENTREGADO (caso, secciones 1 y 3)
public record LeadTimeResponse(
        long entregados,          // envíos entregados en el rango
        double promedioMinutos    // promedio de minutos entre la creación y la entrega (0 si no hubo entregas)
) {}
