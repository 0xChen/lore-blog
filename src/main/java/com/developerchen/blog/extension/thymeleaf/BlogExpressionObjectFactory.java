package com.developerchen.blog.extension.thymeleaf;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.theme.Common;
import com.developerchen.blog.theme.Theme;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author syc
 */
public class BlogExpressionObjectFactory implements IExpressionObjectFactory {

    private static final String BLOG_EVALUATION_VARIABLE_NAME = "blog";
    private static final String CONST_EVALUATION_VARIABLE_NAME = "blogConst";
    private static final String THEME_EVALUATION_VARIABLE_NAME = "theme";


    private static final Set<String> ALL_EXPRESSION_OBJECT_NAMES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    BLOG_EVALUATION_VARIABLE_NAME,
                    CONST_EVALUATION_VARIABLE_NAME,
                    THEME_EVALUATION_VARIABLE_NAME)));

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (BLOG_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return Common.class;
        }
        if (CONST_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return BlogConst.class;
        }
        if (THEME_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return Theme.class;
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return expressionObjectName != null && CONST_EVALUATION_VARIABLE_NAME.equals(expressionObjectName);
    }

}
