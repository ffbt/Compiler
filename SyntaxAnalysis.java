import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;

public class SyntaxAnalysis
{
    private LexicalAnalysis lexicalAnalysis;
    private Token token;
    private int line;
    private int indent;
    protected Draw draw;
    protected ExprNode xOrigin, yOrigin, xScale, yScale, start, end, x, y, rot, step;

    public SyntaxAnalysis(String file)
    {
        draw = new Draw();
        indent = -1;
        line = 1;
        lexicalAnalysis = new LexicalAnalysis(file);
        token = getToken();
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
                token = getToken();
                break;
            case T:
                // token始终使用的是同一个
                exprNode.setToken(token);
                draw.setT(exprNode);
                token = getToken();
                break;
            case L_BRACKET:
                token = getToken();
                exprNode = expression();
                matchToken(TokenType.R_BRACKET);
                break;
            case FUNC:
                Token tmp = token;
                token = getToken();
                matchToken(TokenType.L_BRACKET);
                exprNode.setToken(tmp);
                exprNode.setLeft(expression());
                matchToken(TokenType.R_BRACKET);
                break;
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
            token = getToken();
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
            token = getToken();
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
            token = getToken();
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
            token = getToken();
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
        matchToken(TokenType.T);
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
        matchToken(TokenType.L_BRACKET);
        x = expression();
        printSyntaxTree(x);
        matchToken(TokenType.COMMA);
        y = expression();
        printSyntaxTree(y);
        matchToken(TokenType.R_BRACKET);
        printExitFunction(getMethodName());
        indent--;
    }

    private void matchToken(TokenType tokenType)
    {
        indent++;
        if (token.getType() != tokenType)
            syntaxError(token);
        printMatchState();
        fetchToken();
        indent--;
    }

    private void fetchToken()
    {
        token = getToken();
        if (token.getType() == TokenType.ERRTOKEN)
            syntaxError("Error Token!");
    }

    private void syntaxError(Token token)
    {
        System.out.println("Line " + line + ": " + token.getLexeme() + " 不是预期符号");
        System.exit(-1);
    }

    private void syntaxError(String message)
    {
        System.out.println(message);
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
                Frame frame = new Frame();

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
