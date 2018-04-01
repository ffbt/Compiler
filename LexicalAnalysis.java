import java.io.*;

public class LexicalAnalysis
{
    private PushbackInputStream f = null;

    public static Token tokenString[] = {
            new Token(TokenType.CONST_ID, "PI", Math.PI, null),
            new Token(TokenType.CONST_ID, "E", Math.E, null),
            new Token(TokenType.T, "T", 0.0, null),
            new Token(TokenType.FUNC, "SIN", 0.0, new Function()
            {
                @Override
                public double fun(double param)
                {
                    return Math.sin(param);
                }

                @Override
                public String toString()
                {
                    return "sin";
                }
            }),
            new Token(TokenType.FUNC, "COS", 0.0, new Function()
            {
                @Override
                public double fun(double param)
                {
                    return Math.cos(param);
                }

                @Override
                public String toString()
                {
                    return "cos";
                }
            }),
            new Token(TokenType.FUNC, "TAN", 0.0, new Function()
            {
                @Override
                public double fun(double param)
                {
                    return Math.tan(param);
                }

                @Override
                public String toString()
                {
                    return "tan";
                }
            }),
            new Token(TokenType.FUNC, "LN", 0.0, new Function()
            {
                @Override
                public double fun(double param)
                {
                    return Math.log(param);
                }

                @Override
                public String toString()
                {
                    return "log";
                }
            }),
            new Token(TokenType.FUNC, "EXP", 0.0, new Function()
            {
                @Override
                public double fun(double param)
                {
                    return Math.exp(param);
                }

                @Override
                public String toString()
                {
                    return "exp";
                }
            }),
            new Token(TokenType.FUNC, "SQRT", 0.0, new Function()
            {
                @Override
                public double fun(double param)
                {
                    return Math.sqrt(param);
                }

                @Override
                public String toString()
                {
                    return "sqrt";
                }
            }),
            new Token(TokenType.ORIGIN, "ORIGIN", 0.0, null),
            new Token(TokenType.SCALE, "SCALA", 0.0, null),
            new Token(TokenType.ROT, "ROT", 0.0, null),
            new Token(TokenType.IS, "IS", 0.0, null),
            new Token(TokenType.FOR, "FOR", 0.0, null),
            new Token(TokenType.FROM, "FROM", 0.0, null),
            new Token(TokenType.TO, "TO", 0.0, null),
            new Token(TokenType.STEP, "STEP", 0.0, null),
            new Token(TokenType.DRAW, "DRAW", 0.0, null),
    };

    public static Token tokenSymbol[] = {
            new Token(TokenType.SEMICO, ";", 0.0, null),
            new Token(TokenType.L_BRACKET, "(", 0.0, null),
            new Token(TokenType.R_BRACKET, ")", 0.0, null),
            new Token(TokenType.COMMA, ",", 0.0, null),
            new Token(TokenType.PLUS, "+", 0.0, null),
            /*new Token(TokenType.MINUS, "-", 0.0, null),
            new Token(TokenType.MUL, "*", 0.0, null),
            new Token(TokenType.DIV, "/", 0.0, null),
            new Token(TokenType.POWER, "**", 0.0, null),*/
    };

    public LexicalAnalysis(String file)
    {
        try
        {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            f = new PushbackInputStream(in);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Token getToken()
    {
        Token token = new Token(TokenType.NONTOKEN, "", 0.0, null);
        int c;
        try
        {
            while ((c = f.read()) != -1)
            {
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r')
                    continue;
                if (Character.isLetter(c))
                {
                    String string = String.valueOf((char) c);
                    while ((c = f.read()) != -1 && Character.isLetterOrDigit(c))
                        string += String.valueOf((char) c);
                    if (c != -1)
                        f.unread(c);
                    for (int i = 0; i < tokenString.length; i++)
                    {
                        if ((string.toUpperCase()).equals(tokenString[i].lexeme))
                            return tokenString[i];
                    }
                    token.type = TokenType.ID;
                    token.lexeme = string;
                    return token;
                }
                else if (Character.isDigit(c))
                {
                    String string = String.valueOf((char) c);
                    while ((c = f.read()) != -1 && Character.isDigit(c))
                        string += String.valueOf((char) c);
                    if (c == '.')
                    {
                        string += String.valueOf((char) c);
                        while ((c = f.read()) != -1 && Character.isDigit(c))
                            string += String.valueOf((char) c);
                    }
                    if (c != -1)
                        f.unread(c);
                    token.type = TokenType.CONST_ID;
                    token.value = Double.valueOf(string);
                    return token;
                }
                else
                {
                    for (int i = 0; i < tokenSymbol.length; i++)
                    {
                        if (tokenSymbol[i].lexeme.equals(String.valueOf((char) c)))
                            return tokenSymbol[i];
                        if (c == '*')
                        {
                            if ((c = f.read()) == '*')
                            {
                                token.type = TokenType.POWER;
                                token.lexeme = "**";
                                return token;
                            }
                            if (c != -1)
                                f.unread(c);
                            token.type = TokenType.MUL;
                            token.lexeme = "*";
                            return token;
                        }
                        else if (c == '-')
                        {
                            if ((c = f.read()) == '-')
                            {
                                token.type = TokenType.COMMENT;
                                token.lexeme = "--";
                                return token;
                            }
                            if (c != -1)
                                f.unread(c);
                            token.type = TokenType.MINUS;
                            token.lexeme = "-";
                            return token;
                        }
                        else if (c == '/')
                        {
                            if ((c = f.read()) == '/')
                            {
                                token.type = TokenType.COMMENT;
                                token.lexeme = "//";
                                return token;
                            }
                            if (c != -1)
                                f.unread(c);
                            token.type = TokenType.DIV;
                            token.lexeme = "/";
                            return token;
                        }
                        else
                        {
                            token.type = TokenType.ERRTOKEN;
                            return token;
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return token;
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
            System.out.println("Usage: The name of source file");
        else
        {
            LexicalAnalysis lexicalAnalysis = new LexicalAnalysis(args[0]);
            Token token;
            while ((token = lexicalAnalysis.getToken()).type != TokenType.NONTOKEN)
            {
                System.out.println(token.type + " " + token.lexeme + " " + token.value + " " + token.function);
            }
        }
    }
}
