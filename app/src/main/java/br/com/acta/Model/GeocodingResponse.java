package br.com.acta.Model;

import java.util.List;

public class GeocodingResponse {
    private String status;
    private List<Resultado> results;

    public String getStatus() {
        return status;
    }

    public List<Resultado> getResults() {
        return results;
    }

    public static class Resultado {
        private List<Componente> address_components;

        public List<Componente> getAddressComponents() {
            return address_components;
        }
    }

    public static class Componente {
        private String long_name;
        private List<String> types;

        public String getLongName() {
            return long_name;
        }

        public List<String> getTypes() {
            return types;
        }
    }
}
