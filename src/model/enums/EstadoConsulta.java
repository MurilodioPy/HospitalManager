package model.enums;

public enum EstadoConsulta {

    VAZIO("VAZIO"),
    AGENDADA("AGENDADA"),
    CANCELADA("CANCELADA"),
    REALIZADA("REALIZADA");

    private String estado;

    EstadoConsulta(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return estado;
    }
}
