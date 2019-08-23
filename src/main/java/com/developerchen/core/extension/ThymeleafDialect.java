package com.developerchen.core.extension;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

/**
 * @author syc
 */
public class ThymeleafDialect extends AbstractDialect implements IExpressionObjectDialect {

    private final IExpressionObjectFactory THYMELEAF_EXPRESSION_OBJECT_FACTORY = new ThymeleafExpressionObjectFactory();

    public ThymeleafDialect() {
        super("core");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return THYMELEAF_EXPRESSION_OBJECT_FACTORY;
    }

}
