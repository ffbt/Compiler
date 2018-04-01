import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Interpreter extends SyntaxAnalysis
{
    public Interpreter(String file)
    {
        super(file);
    }

    public Draw getDraw()
    {
        return draw;
    }

    protected void colorStatement()
    {
        super.colorStatement();
        draw.setColor(color);
    }

    protected void originStatement()
    {
        super.originStatement();
        draw.setOrigin(Util.getExprValue(xOrigin), Util.getExprValue(yOrigin));
    }

    protected void rotStatement()
    {
        super.rotStatement();
        draw.setRot(Util.getExprValue(rot));
    }

    protected void scaleStatement()
    {
        super.scaleStatement();
        draw.setScale(Util.getExprValue(xScale), Util.getExprValue(yScale));
    }

    protected void forStatement()
    {
        super.forStatement();
        if (drawPoint)
        {
            draw.setStart(Util.getExprValue(start));
            draw.setEnd(Util.getExprValue(end));
            draw.setStep(Util.getExprValue(step));
            draw.setStepX(x);
            draw.setStepY(y);
            paint();
            circulation();
            drawPoint = false;
        }
    }

    private void paint()
    {
        Graphics g = draw.getGraphics();
        draw.paint(g);
    }

    private void circulation()
    {
        if (!arg.isEmpty())
        {
            String name = arg.poll();
            double a = startEndStep.poll(), b = startEndStep.poll(), c = startEndStep.poll();
            for (double tmp = a; tmp < b; tmp += c)
            {
                map.put(name, tmp);
                paint();
                circulation();
            }
            map.put(name, a);
            arg.offerFirst(name);
            startEndStep.offerFirst(c);
            startEndStep.offerFirst(b);
            startEndStep.offerFirst(a);
        }
    }

    protected void printEnterFunction(String methodName)
    {
    }

    protected void printExitFunction(String methodName)
    {
    }

    protected void printMatchState()
    {
    }

    protected void printSyntaxTree(ExprNode root)
    {
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
            System.out.println("Usage: The name of source file");
        else
        {
            Interpreter interpreter = new Interpreter(args[0]);
            Frame frame = new Frame("Interpreter");
            frame.add(interpreter.getDraw(), BorderLayout.CENTER);
            frame.setSize(600, 600);
            frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    System.exit(0);
                }
            });
            frame.setVisible(true);
            interpreter.program();
            interpreter.getLexicalAnalysis().close();
        }
    }
}
