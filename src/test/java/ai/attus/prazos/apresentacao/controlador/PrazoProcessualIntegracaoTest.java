package ai.attus.prazos.apresentacao.controlador;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Requer PostgreSQL em execução: docker compose up -d
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PrazoProcessualIntegracaoTest {

    @LocalServerPort
    private int porta;

    @BeforeEach
    void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "";
    }

    @Test
    void deveCriarListarBuscarAtualizarEExcluirPrazo() {
        String dataVencimento = LocalDate.now().plusDays(15).toString();

        var respostaCriacao = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "numeroProcesso", "0001234-56.2024.8.26.0100",
                        "descricao", "Prazo integração RestAssured",
                        "dataVencimento", dataVencimento))
                .when()
                .post("/api/v1/prazos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("numeroProcesso", equalTo("0001234-56.2024.8.26.0100"))
                .extract();

        String id = respostaCriacao.path("id");
        Number versao = respostaCriacao.path("versao");

        given()
                .when()
                .get("/api/v1/prazos")
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1));

        given()
                .when()
                .get("/api/v1/prazos/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "numeroProcesso", "0001234-56.2024.8.26.0100",
                        "descricao", "Prazo atualizado via integração",
                        "dataVencimento", dataVencimento,
                        "status", "CONCLUIDO",
                        "versao", versao))
                .when()
                .put("/api/v1/prazos/{id}", id)
                .then()
                .statusCode(200)
                .body("status", equalTo("CONCLUIDO"));

        given()
                .when()
                .delete("/api/v1/prazos/{id}", id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/api/v1/prazos/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void deveRetornarErroValidacaoParaCnjInvalido() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "numeroProcesso", "invalido",
                        "descricao", "Descrição com tamanho válido",
                        "dataVencimento", LocalDate.now().plusDays(3).toString()))
                .when()
                .post("/api/v1/prazos")
                .then()
                .statusCode(400)
                .body("title", equalTo("Regra de negócio violada"));
    }

    @Test
    void deveRetornar404ParaIdInexistente() {
        given()
                .when()
                .get("/api/v1/prazos/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }
}
