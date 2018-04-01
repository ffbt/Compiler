import java.io.*;

public class LexicalAnalysis
{
    private PushbackInputStream f;

    private static Token tokenString[] = {
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
            new Token(TokenType.SCALE, "SCALE", 0.0, null),
            new Token(TokenType.ROT, "ROT", 0.0, null),
            new Token(TokenType.IS, "IS", 0.0, null),
            new Token(TokenType.FOR, "FOR", 0.0, null),
            new Token(TokenType.FROM, "FROM", 0.0, null),
            new Token(TokenType.TO, "TO", 0.0, null),
            new Token(TokenType.STEP, "STEP", 0.0, null),
            new Token(TokenType.DRAW, "DRAW", 0.0, null),
            new Token(TokenType.COLOR, "COLOR", 0.0, null),
    };

    private static Token tokenSymbol[] = {
            new Token(TokenType.SEMICO, ";", 0.0, null),
            new Token(TokenType.L_BRACKET, "(", 0.0, null),
            new Token(TokenType.R_BRACKET, ")", 0.0, null),
            new Token(TokenType.COMMA, ",", 0.0, null),
            new Token(TokenType.PLUS, "+", 0.0, null),
            new Token(TokenType.NEXT_LINE, "\n", 0.0, null),
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

    public void close()
    {
        try
        {
            f.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean isWhiteSpace(int c)
    {
        return c == ' ' || c == '\t' || c == '\r';
    }

    public Token getToken()
    {
        Token token = new Token(TokenType.NONTOKEN, "", 0.0, null);
        int c;
        try
        {
            while ((c = f.read()) != -1)
            {
                if (isWhiteSpace(c))
                    continue;
                StringBuilder string = new StringBuilder("");
                if (Character.isLetter(c))
                {
                    string.append((char) c);
                    while ((c = f.read()) != -1 && Character.isLetterOrDigit(c))
                        string.append((char) c);
                    if (c != -1)
                        f.unread(c);
                    for (Token t : tokenString)
                    {
                        if ((string.toString().toUpperCase()).equals(t.getLexeme()))
                            return t;
                    }
                    token.setType(TokenType.ID);
                    token.setLexeme(string.toString().toUpperCase());
                    return token;
                }
                else if (Character.isDigit(c))
                {
                    string.append((char) c);
                    while ((c = f.read()) != -1 && Character.isDigit(c))
                        string.append((char) c);
                    if (c == '.')
                    {
                        string.append((char) c);
                        while ((c = f.read()) != -1 && Character.isDigit(c))
                            string.append((char) c);
                    }
                    if (c != -1)
                        f.unread(c);
                    token.setType(TokenType.CONST_ID);
                    token.setLexeme(string.toString());
                    token.setValue(Double.parseDouble(string.toString()));
                    return token;
                }
                else
                {
                    for (Token t : tokenSymbol)
                    {
                        if (t.getLexeme().equals(String.valueOf((char) c)))
                            return t;
                    }
                    if (c == '*')
                    {
                        if ((c = f.read()) == '*')
                        {
                            token.setType(TokenType.POWER);
                            token.setLexeme("**");
                            return token;
                        }
                        if (c != -1)
                            f.unread(c);
                        token.setType(TokenType.MUL);
                        token.setLexeme("*");
                        return token;
                    }
                    else if (c == '-')
                    {
                        if ((c = f.read()) == '-')
                        {
                            token.setType(TokenType.COMMENT);
                            token.setLexeme("--");
                            return token;
                        }
                        if (c != -1)
                            f.unread(c);
                        token.setType(TokenType.MINUS);
                        token.setLexeme("-");
                        return token;
                    }
                    else if (c == '/')
                    {
                        if ((c = f.read()) == '/')
                        {
                            token.setType(TokenType.COMMENT);
                            token.setLexeme("//");
                            return token;
                        }
                        if (c != -1)
                            f.unread(c);
                        token.setType(TokenType.DIV);
                        token.setLexeme("/");
                        return token;
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
            try
            {
                System.setOut(new PrintStream("LexicalOut.txt"));
                LexicalAnalysis lexicalAnalysis = new LexicalAnalysis(args[0]);
                Token token;
                while ((token = lexicalAnalysis.getToken()).getType() != TokenType.NONTOKEN)
                {
                    System.out.println(token.getType() + " " + token.getLexeme() + " " + token.getValue() + " " + token.getFunction());
                }
                lexicalAnalysis.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
