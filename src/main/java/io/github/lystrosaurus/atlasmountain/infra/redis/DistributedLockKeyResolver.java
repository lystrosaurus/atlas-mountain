package io.github.lystrosaurus.atlasmountain.infra.redis;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockKeyResolver {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext parserContext = new TemplateParserContext();

    public String resolve(String expression, StandardEvaluationContext context) {
        return parser.parseExpression(expression, parserContext).getValue(context, String.class);
    }
}
