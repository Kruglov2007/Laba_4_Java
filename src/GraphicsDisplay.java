import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;

public class GraphicsDisplay extends JPanel {
    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = false;
    private boolean showExtremes = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Шрифт отображения подписей к осям координат
    private Font axisFont;

    public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[]{4.0f, 1.0f, 4.0f, 1.0f, 4.0f,1.0f, 1.0f, 1.0f, 1.0f, 1.0f,1.0f}, 0.0f);
        // Перо для рисования осей координат
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);

    }
    // Метод вызывается из обработчика элемента меню "Открыть файл с графиком"
// главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
// Запросить перерисовку компонента (неявно вызвать paintComponent())
        repaint();
    }
    // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }
    public void setShowExtremes(boolean showExtremes){
        this.showExtremes=showExtremes;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
// Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
// Задать её координаты как координаты существующей точки +
// заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    protected void paintGraphics(Graphics2D canvas) {
// Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
        // Выбрать цвет линии
        canvas.setColor(Color.RED);
/* Будем рисовать линию графика как путь, состоящий из множества
сегментов (GeneralPath). Начало пути устанавливается в первую точку
графика, после чего прямой соединяется со следующими точками */
        GeneralPath graphics = new GeneralPath();
        for (int i=0; i<graphicsData.length; i++) {
// Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i>0) {
// Не первая итерация – вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
// Первая итерация - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
// Отобразить график
        canvas.draw(graphics);
    }

    protected void paintAxis(Graphics2D canvas) {
// Шаг 1 – установить необходимые настройки рисования
// Установить особое начертание для осей
        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения
// характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
// Шаг 2 - Определить, должна ли быть видна ось Y на графике
        if (minX<=0.0 && maxX>=0.0) {
// Она видна, если левая граница показываемой области minX<=0.0,
// а правая (maxX) >= 0.0
// Шаг 2а - ось Y - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
// Шаг 2б - Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными
// координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX()+5,
                    arrow.getCurrentPoint().getY()+20);
// Вести нижнюю часть стрелки в точку с относительными
// координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX()-10,
                    arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Шаг 2в - Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи “y”
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float)labelPos.getX() + 10,
                    (float)(labelPos.getY() - bounds.getY()));
        }
// Шаг 3 - Определить, должна ли быть видна ось X на графике
        if (minY<=0.0 && maxY>=0.0) {
// Она видна, если верхняя граница показываемой области max)>=0.0,
// а нижняя (minY) <= 0.0
// Шаг 3а - ось X - это линия между точками (minX, 0) и (maxX, 0)
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
// Шаг 3б - Стрелка оси X
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными
// координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX()-20,
                    arrow.getCurrentPoint().getY()-5);
// Вести левую часть стрелки в точку
// с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY()+10);
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Шаг 3в - Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи “x”
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x",
                    (float)(labelPos.getX()-bounds.getWidth()-10),
                    (float)(labelPos.getY() + bounds.getY()));
        }
        if (showExtremes) {
            // 1. Обозначаем точку (0,0) ТОЛЬКО если она входит в диапазон видимости
            if (minX <= 0.0 && maxX >= 0.0 && minY <= 0.0 && maxY >= 0.0) {
                Point2D.Double centerPoint = xyToPoint(0, 0);
                canvas.setColor(Color.GREEN); // Выделим зеленым цветом точку нуля
                canvas.setPaint(Color.GREEN);
                canvas.fill(new Ellipse2D.Double(centerPoint.getX() - 4, centerPoint.getY() - 4, 8, 8));

                canvas.setFont(new Font("Serif", Font.BOLD, 14));
                canvas.drawString("0,0", (float) centerPoint.getX() + 5, (float) centerPoint.getY() - 5);
            }

            // 2. А крайние значения на осях подписываем В ЛЮБОМ СЛУЧАЕ (вне зависимости от 0,0)
            canvas.setColor(Color.DARK_GRAY);
            canvas.setPaint(Color.DARK_GRAY);
            Font smallFont = new Font("Serif", Font.PLAIN, 16);
            canvas.setFont(smallFont);

            Point2D.Double pMinX = xyToPoint(minX, 0);
            canvas.drawString(String.format("%.2f", minX), (float) pMinX.getX(), (float) pMinX.getY() + 20);

            Point2D.Double pMaxX = xyToPoint(maxX, 0);
            canvas.drawString(String.format("%.2f", maxX), (float) pMaxX.getX() - 40, (float) pMaxX.getY() + 20);

            Point2D.Double pMinY = xyToPoint(0, minY);
            canvas.drawString(String.format("%.2f", minY), (float) pMinY.getX() + 10, (float) pMinY.getY());

            Point2D.Double pMaxY = xyToPoint(0, maxY);
            canvas.drawString(String.format("%.2f", maxY), (float) pMaxY.getX() + 10, (float) pMaxY.getY() + 15);
        }


}
    // Проверяет, является ли целая часть значения функции Y чётной
    private boolean isEvenIntegerPart(double y) {
        long intPart = (long) Math.floor(Math.abs(y));
        return intPart % 2 == 0;
    }
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        for (Double[] point : graphicsData) {
            double yValue = point[1];
            if (isEvenIntegerPart(yValue)){
                canvas.setColor(Color.BLUE);
                canvas.setPaint(Color.BLUE);
            }else {
                canvas.setColor(Color.RED);
                canvas.setPaint(Color.RED);
            }
            Point2D.Double center = xyToPoint(point[0], point[1]);
            Ellipse2D.Double circle=new Ellipse2D.Double(center.x-5.5, center.y-5.5, 11,11);
            Line2D.Double horizLine=new Line2D.Double(center.getX() - 5.5, center.getY(),
                    center.getX() + 5.5, center.getY());
            Line2D.Double vertLine=new Line2D.Double(center.getX() , center.getY()-5.5,
                    center.getX(), center.getY()+5.5);
            canvas.draw(circle);
            canvas.draw(horizLine);
            canvas.draw(vertLine);

        }

    }

    public void paintComponent(Graphics g) {
        /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
         * Эта функциональность - единственное, что осталось в наследство от
         * paintComponent класса JPanel
         */
        super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при
// запуске программы) - ничего не делать
        if (graphicsData==null || graphicsData.length==0) return;
// Шаг 3 - Определить начальные границы области отображения
// Еѐ верхний левый угол - (minX, maxY), правый нижний - (maxX, minY)
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];
        minY = graphicsData[0][1];
        maxY = minY;
// Найти минимальное и максимальное значение функции
        for (int i = 1; i<graphicsData.length; i++) {
            if (graphicsData[i][1]<minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1]>maxY) {
                maxY = graphicsData[i][1];
            }
        }
/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y –
сколько пикселов приходится на единицу длины по X и по Y */
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
// Выбрать единый масштаб как минимальный из двух
        scale = Math.min(scaleX, scaleY);
// Шаг 5 - корректировка границ области согласно выбранному масштабу
        if (scale==scaleX) {
/* Если за основу был взят масштаб по оси X, значит по оси Y
делений меньше, т.е. подлежащий отображению диапазон по Y будет меньше
высоты окна. Значит необходимо добавить делений, сделаем это так:
1) Вычислим, сколько делений влезет по Y при выбранном масштабе -
getSize().getHeight()/scale;
2) Вычтем из этого значения сколько делений требовалось изначально;
3) Набросим по половине недостающего расстояния на maxY и minY */
            double yIncrement = (getSize().getHeight()/scale-(maxY-minY))/2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale==scaleY) {
// Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement = (getSize().getWidth()/scale-(maxX-minX))/2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
// Шаг 5 – Преобразовать экземпляр Graphics к Graphics2D
        Graphics2D canvas = (Graphics2D) g;
// Шаг 6 - Сохранить текущие настройки холста
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет
// затираться последующим
// Первым (если нужно) отрисовываются оси координат.
        if (showAxis) paintAxis(canvas);
// Затем отображается сам график
        paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек графика.
        if (showMarkers) paintMarkers(canvas);
// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }






}
