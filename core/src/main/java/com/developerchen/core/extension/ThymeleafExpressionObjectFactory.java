package com.developerchen.core.extension;

import com.developerchen.core.constant.Const;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author syc
 */
public class ThymeleafExpressionObjectFactory implements IExpressionObjectFactory {

    private static final String COMMON_EVALUATION_VARIABLE_NAME = "common";
    private static final String CONST_EVALUATION_VARIABLE_NAME = "const";

    private static final Set<String> ALL_EXPRESSION_OBJECT_NAMES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    COMMON_EVALUATION_VARIABLE_NAME,
                    CONST_EVALUATION_VARIABLE_NAME)));

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (COMMON_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return null;
        }
        if (CONST_EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return Const.class;
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return expressionObjectName != null;
    }

}
