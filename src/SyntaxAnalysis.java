import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class SyntaxAnalysis
{
    private LexicalAnalysis lexicalAnalysis;
    private Token token;
    private int line;
    private int indent;
    protected Draw draw;
    protected ExprNode xOrigin, yOrigin, xScale, yScale, start, end, x, y, rot, step;
    protected static HashMap<String, Double> map;
    protected boolean drawPoint;
    protected Deque<String> arg;
    protected Deque<Double> startEndStep;
    protected Color color;

    public static HashMap<String, Double> getMap()
    {
        return map;
    }

    public SyntaxAnalysis(String file)
    {
        arg = new LinkedBlockingDeque<>();
        startEndStep = new LinkedBlockingDeque<>();
        map = new HashMap<>();
        draw = new Draw();
        indent = -1;
        line = 1;
        lexicalAnalysis = new LexicalAnalysis(file);
        fetchToken();
    }

    public LexicalAnalysis getLexicalAnalysis()
    {
        return lexicalAnalysis;
    }

    private Token getToken()
    {
        while ((token = lexicalAnalysis.getToken()).getType() == TokenType.NEXT_LINE)
            line++;
        while (token.getType() == TokenType.COMMENT)
        {
            while ((token = lexicalAnalysis.getToken()).getType() != TokenType.NEXT_LINE && token.getType() != TokenType.NONTOKEN)
                ;
            while (token.getType() == TokenType.NEXT_LINE)
            {
                line++;
                token = lexicalAnalysis.getToken();
            }
        }
        return token;
    }

    private ExprNode atom()
    {
        ExprNode exprNode = new ExprNode();
        switch (token.getType())
        {
            case CONST_ID:
                exprNode.setToken(token);
                fetchToken();
                break;
            case T:
                exprNode = draw.getT();
                fetchToken();
                break;
            case L_BRACKET:
                fetchToken();
                exprNode = expression();
                matchToken(TokenType.R_BRACKET);
                break;
            case FUNC:
                Token tmp = token;
                fetchToken();
                matchToken(TokenType.L_BRACKET);
                exprNode.setToken(tmp);
                exprNode.setLeft(expression());
                matchToken(TokenType.R_BRACKET);
                break;
            case ID:
                if (map.containsKey(token.getLexeme()))
                {
                    exprNode.setToken(token);
                    fetchToken();
                    break;
                }
                else
                    syntaxError(token.getLexeme() + " hasn't been defined");
            default:
                break;
        }
        return exprNode;
    }

    private ExprNode component()
    {
        ExprNode exprNode = atom();
        if (token.getType() == TokenType.POWER)
        {
            Token tmp = token;
            fetchToken();
            exprNode = new ExprNode(tmp, exprNode, component());
        }
        return exprNode;
    }

    private ExprNode factor()
    {
        if (token.getType() == TokenType.PLUS)
            return factor();
        if (token.getType() == TokenType.MINUS)
        {
            Token tmp = token;
            fetchToken();
            return new ExprNode(tmp,
                    new ExprNode(new Token(TokenType.CONST_ID, "0", 0.0, null), null, null),
                    factor());
        }
        return component();
    }

    private ExprNode term()
    {
        ExprNode exprNode = factor();
        while (token.getType() == TokenType.MUL || token.getType() == TokenType.DIV)
        {
            Token tmp = token;
            fetchToken();
            exprNode = new ExprNode(tmp, exprNode, factor());
        }
        return exprNode;
    }

    private ExprNode expression()
    {
        indent++;
        printEnterFunction(getMethodName());
        ExprNode exprNode = term();
        while (token.getType() == TokenType.PLUS || token.getType() == TokenType.MINUS)
        {
            Token tmp = token;
            fetchToken();
            exprNode = new ExprNode(tmp, exprNode, term());
        }
        printExitFunction(getMethodName());
        indent--;
        return exprNode;
    }

    private void statement()
    {
        indent++;
        printEnterFunction(getMethodName());
        switch (token.getType())
        {
            case ORIGIN:
                originStatement();
                break;
            case SCALE:
                scaleStatement();
                break;
            case ROT:
                rotStatement();
                break;
            case FOR:
                forStatement();
                break;
            case ID:
                defineStatement();
                break;
            case COLOR:
                colorStatement();
                break;
            default:
                syntaxError(token);
                break;
        }
        printExitFunction(getMethodName());
        indent--;
    }

    public void program()
    {
        indent++;
        printEnterFunction(getMethodName());
        while (token.getType() != TokenType.NONTOKEN)
        {
            statement();
            matchToken(TokenType.SEMICO);
        }
        printExitFunction(getMethodName());
        indent--;
    }

    protected void colorStatement()
    {
        indent++;
        printEnterFunction(getMethodName());
        matchToken(TokenType.COLOR);
        matchToken(TokenType.IS);
        if (token.getType() == TokenType.ID)
        {
            switch (token.getLexeme())
            {
                case "RED":
                    color = Color.RED;
                    break;
                case "BLUE":
                    color = Color.BLUE;
                    break;
                case "BLACK":
                    color = Color.BLACK;
                    break;
                case "CYAN":
                    color = Color.CYAN;
                    break;
                case "GRAY":
                    color = Color.GRAY;
                    break;
                case "GREEN":
                    color = Color.GREEN;
                    break;
                case "YELLOW":
                    color = Color.YELLOW;
                    break;
                case "ORANGE":
                    color = Color.ORANGE;
                    break;
                default:
                    syntaxError(token.getLexeme() + ": unknown color");
                    break;
            }
            matchToken(TokenType.ID);
        }
        else
            syntaxError(token);
        printExitFunction(getMethodName());
        indent--;
    }

    private void defineStatement()
    {
        indent++;
        printEnterFunction(getMethodName());
        String tmp = token.getLexeme();
        matchToken(TokenType.ID);
        matchToken(TokenType.IS);
        if (token.getType() == TokenType.CONST_ID)
        {
            map.put(tmp, token.getValue());
            matchToken(TokenType.CONST_ID);
        }
        else
            syntaxError(token);
        printExitFunction(getMethodName());
        indent--;
    }

    protected void originStatement()
    {
        indent++;
        printEnterFunction(getMethodName());
        matchToken(TokenType.ORIGIN);
        matchToken(TokenType.IS);
        matchToken(TokenType.L_BRACKET);
        xOrigin = expression();
        printSyntaxTree(xOrigin);
        matchToken(TokenType.COMMA);
        yOrigin = expression();
        printSyntaxTree(yOrigin);
        matchToken(TokenType.R_BRACKET);
        printExitFunction(getMethodName());
        indent--;
    }

    protected void rotStatement()
    {
        indent++;
        printEnterFunction(getMethodName());
        matchToken(TokenType.ROT);
        matchToken(TokenType.IS);
        rot = expression();
        printSyntaxTree(rot);
        printExitFunction(getMethodName());
        indent--;
    }

    protected void scaleStatement()
    {
        indent++;
        printEnterFunction(getMethodName());
        matchToken(TokenType.SCALE);
        matchToken(TokenType.IS);
        matchToken(TokenType.L_BRACKET);
        xScale = expression();
        printSyntaxTree(xScale);
        matchToken(TokenType.COMMA);
        yScale = expression();
        printSyntaxTree(yScale);
        matchToken(TokenType.R_BRACKET);
        printExitFunction(getMethodName());
        indent--;
    }

    protected void forStatement()
    {
        indent++;
        printEnterFunction(getMethodName());
        matchToken(TokenType.FOR);
        if (token.getType() == TokenType.T)
            matchToken(TokenType.T);
        else if (token.getType() == TokenType.ID)
        {
            arg.offer(token.getLexeme());
            map.put(token.getLexeme(), 0.0);
            matchToken(TokenType.ID);
        }
        else
            syntaxError(token);
        matchToken(TokenType.FROM);
        start = expression();
        printSyntaxTree(start);
        matchToken(TokenType.TO);
        end = expression();
        printSyntaxTree(end);
        matchToken(TokenType.STEP);
        step = expression();
        printSyntaxTree(step);
        matchToken(TokenType.DRAW);
        if (token.getType() == TokenType.L_BRACKET)
        {
            drawPoint = true;
            matchToken(TokenType.L_BRACKET);
            x = expression();
            printSyntaxTree(x);
            matchToken(TokenType.COMMA);
            y = expression();
            printSyntaxTree(y);
            matchToken(TokenType.R_BRACKET);
        }
        else if (token.getType() == TokenType.FOR)
        {
            drawPoint = false;
            startEndStep.offer(Util.getExprValue(start));
            startEndStep.offer(Util.getExprValue(end));
            startEndStep.offer(Util.getExprValue(step));
            forStatement();
        }
        else
            syntaxError(token);
        printExitFunction(getMethodName());
        indent--;
    }

    private void matchToken(TokenType tokenType)
    {
        indent++;
        if (token.getType() != tokenType)
            syntaxError(token);
        if (token.getType() == TokenType.T)
            draw.setT(new ExprNode(token, null, null));
        printMatchState();
        fetchToken();
        indent--;
    }

    private void fetchToken()
    {
        token = getToken();
        if (token.getType() == TokenType.ERRTOKEN)
            syntaxError(token.getLexeme() + " is error token");
    }

    private void syntaxError(Token token)
    {
        System.out.println("Line " + line + ": " + token.getLexeme() + " 不是预期符号");
        System.exit(-1);
    }

    private void syntaxError(String message)
    {
        System.out.println("Line " + line + ": " + message);
        System.exit(-1);
    }

    private String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    protected void printEnterFunction(String methodName)
    {
        for (int i = 0; i < indent; i++)
            System.out.print("\t");
        System.out.println("enter in " + methodName);
    }

    protected void printExitFunction(String methodName)
    {
        for (int i = 0; i < indent; i++)
            System.out.print("\t");
        System.out.println("exit from " + methodName);
    }

    protected void printMatchState()
    {
        for (int i = 0; i < indent; i++)
            System.out.print("\t");
        System.out.println("matchtoken " + token.getLexeme());
    }

    protected void printSyntaxTree(ExprNode root)
    {
        indent++;
        if (root != null)
        {
            for (int i = 0; i < indent; i++)
                System.out.print("\t");
            System.out.println(root.getToken().getLexeme());
            printSyntaxTree(root.getLeft());
            printSyntaxTree(root.getRight());
        }
        indent--;
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
            System.out.println("Usage: The name of source file");
        else
        {
            try
            {
                System.setOut(new PrintStream("SyntaxOut.txt"));
                SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis(args[0]);

                syntaxAnalysis.program();
                syntaxAnalysis.getLexicalAnalysis().close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
