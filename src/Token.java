public class Token
{
    private TokenType type;
    private String lexeme;
    private double value;
    private Function function;

    public Token(TokenType type, String lexeme, double value, Function function)
    {
        this.type = type;
        this.lexeme = lexeme;
        this.value = value;
        this.function = function;
    }

    public double getValue()
    {
        return value;
    }

    public Function getFunction()
    {
        return function;
    }

    public String getLexeme()
    {
        return lexeme;
    }

    public TokenType getType()
    {
        return type;
    }

    public void setFunction(Function function)
    {
        this.function = function;
    }

    public void setLexeme(String lexeme)
    {
        this.lexeme = lexeme;
    }

    public void setType(TokenType type)
    {
        this.type = type;
    }

    public void setValue(double value)
    {
        this.value = value;
    }
}
