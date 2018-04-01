import java.awt.*;

public class Draw extends Panel
{
    private double x = 0, y = 0;
    private double xScale = 1, yScale = 1;
    private double step;
    private double start, end;
    private double rot = 0;
    private ExprNode t;
    private ExprNode stepX, stepY;
    private Color color;

    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        if (color != null)
            g.setColor(color);
        for (double a = start; a <= end; a += step)
        {
            long time = System.currentTimeMillis();
            t.getToken().setValue(a);
            int cx = calcX();
            int cy = calcY();
            g2d.drawLine(cx, 600 - cy, cx, 600 - cy);
            while (System.currentTimeMillis() - time < 2);
        }
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public ExprNode getT()
    {
        return t;
    }

    private int calcX()
    {
        return (int) (Util.getExprValue(stepX) * xScale * Math.cos(rot) + Util.getExprValue(stepY) * yScale * Math.sin(rot) + x);
    }

    private int calcY()
    {
        return (int) (Util.getExprValue(stepY) * yScale * Math.cos(rot) - Util.getExprValue(stepX) * xScale * Math.sin(rot) + y);
    }

    public void setStepX(ExprNode stepX)
    {
        this.stepX = stepX;
    }

    public void setStepY(ExprNode stepY)
    {
        this.stepY = stepY;
    }

    public void setOrigin(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public void setT(ExprNode t)
    {
        this.t = t;
    }

    public void setStep(double step)
    {
        this.step = step;
    }

    public void setScale(double x, double y)
    {
        xScale = x;
        yScale = y;
    }

    public void setStart(double start)
    {
        this.start = start;
    }

    public void setEnd(double end)
    {
        this.end = end;
    }

    public void setRot(double rot)
    {
        this.rot = 2 * Math.PI - rot;
    }

    public static void main(String[] args)
    {

    }
}
