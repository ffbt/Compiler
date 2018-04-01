public class Util
{
    public static double getExprValue(ExprNode root)
    {
        double right;
        if (root == null)
            return 0.0;
        switch (root.getToken().getType())
        {
            case PLUS:
                return getExprValue(root.getLeft()) + getExprValue(root.getRight());
            case MINUS:
                return getExprValue(root.getLeft()) - getExprValue(root.getRight());
            case MUL:
                return getExprValue(root.getLeft()) * getExprValue(root.getRight());
            case DIV:
                if ((right = getExprValue(root.getRight())) == 0)
                    return 0.0;
                return getExprValue(root.getLeft()) / right;
            case POWER:
                return Math.pow(getExprValue(root.getLeft()), getExprValue(root.getRight()));
            case FUNC:
                return root.getToken().getFunction().fun(getExprValue(root.getLeft()));
            case T:
            case CONST_ID:
                return root.getToken().getValue();
            case ID:
                return Interpreter.getMap().get(root.getToken().getLexeme());
            default:
                return 0.0;
        }
    }
}
