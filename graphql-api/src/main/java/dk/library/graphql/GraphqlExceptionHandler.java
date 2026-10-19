package dk.library.graphql;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
@Component
public class GraphqlExceptionHandler extends DataFetcherExceptionResolverAdapter {
    @Override protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ApiException api) {
            return GraphqlErrorBuilder.newError(env).message(api.getMessage())
                .errorType(api.code().equals("NOT_FOUND") ? ErrorType.NOT_FOUND : ErrorType.BAD_REQUEST)
                .extensions(Map.of("code", api.code())).build();
        }
        // Spring logs unexpected failures and returns a masked INTERNAL_ERROR.
        return null;
    }
}
