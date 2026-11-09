package com.uagrm.comedor.menu.infrastructure.graphql;

import com.uagrm.comedor.menu.application.dto.InsumoDto;
import com.uagrm.comedor.menu.application.dto.MenuDto;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.data.federation.FederationSchemaFactory;
import org.springframework.graphql.execution.ClassNameTypeResolver;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;

/** Convierte el esquema en un subgrafo de Apollo Federation y deja que los errores de negocio lleguen con su motivo. */
@Configuration
public class FederacionConfig {

    @Bean
    public FederationSchemaFactory federationSchemaFactory() {
        // Las entidades salen como DTO: se indica a qué tipo de GraphQL corresponde cada uno
        ClassNameTypeResolver tipos = new ClassNameTypeResolver();
        tipos.addMapping(MenuDto.class, "Menu");
        tipos.addMapping(InsumoDto.class, "Insumo");
        FederationSchemaFactory factory = new FederationSchemaFactory();
        factory.setTypeResolver(tipos);
        return factory;
    }

    @Bean
    public GraphQlSourceBuilderCustomizer subgrafoFederado(FederationSchemaFactory factory) {
        return builder -> builder.schemaFactory(factory::createGraphQLSchema);
    }

    @Bean
    public DataFetcherExceptionResolver erroresDeNegocio() {
        return new DataFetcherExceptionResolverAdapter() {
            @Override
            protected GraphQLError resolveToSingleError(Throwable e, DataFetchingEnvironment env) {
                if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                    return GraphqlErrorBuilder.newError(env).message(e.getMessage()).build();
                }
                return null;              // lo inesperado no expone detalles internos
            }
        };
    }
}
