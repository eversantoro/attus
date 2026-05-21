package ai.attus.prazos.infraestrutura.configuracao;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoOpenApi {

    @Bean
    public OpenAPI documentacaoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gerenciador de Prazos Processuais - Attus")
                        .description("API REST para gestão de prazos processuais no ecossistema de procuradorias.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Attus Procuradoria Digital")
                                .url("https://www.attus.ai/")));
    }
}
