package com.example.table;

import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.R.integer;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

	SensorManager sensorManager = null;
	Sensor sensor = null;

	final int size = 1024;
	double[] x = new double[size];
	double[] y = new double[size];
	double[] z = new double[size];
	int xIndex = 0;
	int yIndex = 0;
	int zIndex = 0;

	private GraphicalView chart;
	private CategorySeries series1;
	private CategorySeries series2;
	private CategorySeries series3;
	private XYMultipleSeriesDataset dataset;

	private Timer timer = new Timer();
	private TimerTask task;
	private static Handler handler;

	private enum Status {
		Higher, Normal, Lower
	}

	private Status firstStatus = Status.Normal;
	private Status secondStatus = Status.Normal;

	private float xAvg = 0;
	private float yAvg = 0;
	private float zAvg = 0;
	private float xSum = 0;
	private float ySum = 0;
	private float zSum = 0;
	private boolean initialized = true;
	private final int initialTotalTimes = 500;
	private int initialTimes = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);

		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
		// 生成图表
		chart = ChartFactory.getCubeLineChartView(this, getDateDemoDataset(),
				getDemoRenderer(), 0.5f);
		layout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT, 380));

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 刷新图表
				buildBarDataset();
				chart.invalidate();
				super.handleMessage(msg);
			}
		};
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 200;
				handler.sendMessage(message);
			}
		};
		timer.schedule(task, 2000, 500);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			addX(event.values[0]);
			addY(event.values[1]);
			addZ(event.values[2]);

			if (!initialized) {
				if (initialTimes < initialTotalTimes && initialTimes > 100) {
					xSum += event.values[0];
					ySum += event.values[1];
					zSum += event.values[2];
				} else if (initialTimes >= initialTotalTimes) {
					xAvg = xSum / initialTotalTimes;
					yAvg = ySum / initialTotalTimes;
					zAvg = zSum / initialTotalTimes;
					initialTimes = 0;
					initialized = true;
					Toast.makeText(this, "Initial finished", Toast.LENGTH_SHORT)
							.show();
				}
				initialTimes++;
			}
		}
		// Log.e("tag", "x: " + event.values[0] + " y: " + event.values[1]
		// + " z: " + event.values[2]);
	}

	private void addX(float x) {
		if (xIndex >= size) {
			for (int i = 0; i < size; i++)
				this.x[i] = 0;
			xIndex = 0;
		}
		this.x[xIndex++] = x;
	}

	private void addY(float y) {
		if (yIndex >= size) {
			for (int i = 0; i < size; i++)
				this.y[i] = 0;
			yIndex = 0;
		}
		this.y[yIndex++] = y;

		// Log.e("tag", "" + y);
		if (yIndex > 3 && yIndex < size && initialized) {
			TextView textView = (TextView) findViewById(R.id.textView1);

			if (this.y[yIndex] - yAvg <= -0.01) {
				if (firstStatus == Status.Normal)
					firstStatus = Status.Lower;
				else if (firstStatus == Status.Higher)
					secondStatus = Status.Lower;
			} else if (this.y[yIndex] - yAvg >= 0.01) {
				if (firstStatus == Status.Normal)
					firstStatus = Status.Higher;
				else if (firstStatus == Status.Lower)
					secondStatus = Status.Higher;
			}

			if (firstStatus == Status.Lower && secondStatus == Status.Higher) {
				textView.setText("Left");
				firstStatus = Status.Normal;
				secondStatus = Status.Normal;
			} else if (firstStatus == Status.Higher
					&& secondStatus == Status.Lower) {
				textView.setText("Right");
				firstStatus = Status.Normal;
				secondStatus = Status.Normal;
			}

			// if (this.y[yIndex] - this.y[yIndex - 2] <= -0.01
			// && this.y[yIndex - 1] - this.y[yIndex - 2] >= 0.01)
			// textView.setText("Left");
			// else if (this.y[yIndex] - this.y[yIndex - 2] >= 0.01
			// && this.y[yIndex - 1] - this.y[yIndex - 2] <= -0.01)
			// textView.setText("Right");
			// else
			// textView.setText("Nothing");
		}
	}

	private void addZ(float z) {
		if (zIndex >= size) {
			for (int i = 0; i < size; i++)
				this.z[i] = 0;
			zIndex = 0;
		}
		this.z[zIndex++] = z;
	}

	protected void buildBarDataset() {
		dataset.clear();
		series1 = new CategorySeries("X");
		series2 = new CategorySeries("Y");
		series3 = new CategorySeries("Z");
		int seriesLength = x.length;
		for (int k = 0; k < seriesLength; k++) {
			series1.add(x[k]);
		}
		seriesLength = y.length;
		for (int k = 0; k < seriesLength; k++) {
			series2.add(y[k]);
		}
		seriesLength = z.length;
		for (int k = 0; k < seriesLength; k++) {
			series3.add(z[k]);
		}
		dataset.addSeries(series1.toXYSeries());
		dataset.addSeries(series2.toXYSeries());
		dataset.addSeries(series3.toXYSeries());
	}

	private XYMultipleSeriesRenderer getDemoRenderer() {
		int[] colors = new int[] { Color.RED, Color.GREEN, Color.BLUE };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT,
				PointStyle.POINT, PointStyle.POINT };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, /* 渲染器 */
				"Record", /* 图表标题 */
				"Index", /* x轴标题 */
				"Value", /* y轴标题 */
				0, /* x轴最小值 */
				size, /* x轴最大值 */
				-0.03, /* y轴最小值 */
				0.03, /* y轴最大值 */
				Color.GRAY, /* 坐标轴颜色 */
				Color.LTGRAY); /* 标签颜色 标签即 图表标题 xy轴标题 */

		renderer.setXLabels(12); /* 设置 x 轴刻度个数 */
		renderer.setYLabels(10); /* 设置 y 轴刻度个数 */
		renderer.setChartTitleTextSize(20); /* 设置表格标题字体大小 */
		renderer.setTextTypeface("sans_serif", Typeface.BOLD); /* 设置字体 */
		renderer.setLabelsTextSize(14f); /* 设置字体大小 */
		renderer.setAxisTitleTextSize(15);
		renderer.setLegendTextSize(15);
		return renderer;
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}

	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors,
			PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	private XYMultipleSeriesDataset getDateDemoDataset() {
		series1 = new CategorySeries("X");
		series2 = new CategorySeries("Y");
		series3 = new CategorySeries("Z");
		dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series1.toXYSeries());
		dataset.addSeries(series2.toXYSeries());
		dataset.addSeries(series3.toXYSeries());
		return dataset;
	}

	public void Initial(View v) {
		initialized = false;
	}
}
