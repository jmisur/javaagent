package org.springframework.security.access.expression.method;

import org.springframework.security.access.ConfigAttribute;

public class ExpressionAttributeHelper {
    public static String getExpression(ConfigAttribute attr) {
        if (attr instanceof PreInvocationExpressionAttribute
                || attr instanceof PostInvocationExpressionAttribute)
            return ((PreInvocationExpressionAttribute) attr).getAuthorizeExpression().getExpressionString();

        return attr.getAttribute();
    }
}
