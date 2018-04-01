public class ExprNode
{
    private Token token;
    private ExprNode left;
    private ExprNode right;

    public ExprNode()
    {
        token = null;
        left = null;
        right = null;
    }

    public ExprNode(Token token, ExprNode left, ExprNode right)
    {
        this.token = token;
        this.left = left;
        this.right = right;
    }

    public ExprNode getLeft()
    {
        return left;
    }

    public ExprNode getRight()
    {
        return right;
    }

    public Token getToken()
    {
        return token;
    }

    public void setLeft(ExprNode left)
    {
        this.left = left;
    }

    public void setToken(Token token)
    {
        this.token = token;
    }

    public void setRight(ExprNode right)
    {
        this.right = right;
    }
}
