package com.developerchen.blog.extension.thymeleaf;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

/**
 * @author syc
 */
public class BlogDialect extends AbstractDialect implements IExpressionObjectDialect {

    private final IExpressionObjectFactory BLOG_EXPRESSION_OBJECT_FACTORY = new BlogExpressionObjectFactory();

    public BlogDialect() {
        super("blog");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return BLOG_EXPRESSION_OBJECT_FACTORY;
    }

}
