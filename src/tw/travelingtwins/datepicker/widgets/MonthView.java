package tw.travelingtwins.datepicker.widgets;

import java.util.Calendar;
import java.util.Date;

import tw.travelingtwins.datepicker.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class MonthView extends LinearLayout {
	private Context mContext;

	private final String[] mMonths = getResources().getStringArray(R.array.months);

	private Calendar mCurrentMonth = Calendar.getInstance();
	private Calendar mDateSelected = Calendar.getInstance();
	private boolean mMoveToNextMonth = false;

	private ViewSwitcher mViewSwitcher;
	private Animation mInAnimationLastMonth, mInAnimationNextMonth, mOutAnimationLastMonth,
			mOutAnimationNextMonth;

	private TextView mPreviousSelectedDate;
	private RelativeLayout mMonthIndicator;

	public interface OnDateSelectedListener {
		public void onDateSelected(Calendar date);
	}
	
	private OnDateSelectedListener mDateSelectedListener;
	
	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MonthView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.month_view, this);

		mMonthIndicator = (RelativeLayout) findViewById(R.id.month_indicator);

		((ImageButton) mMonthIndicator.findViewById(R.id.last_month))
				.setOnClickListener(ChangeMonthListener);
		((ImageButton) mMonthIndicator.findViewById(R.id.next_month))
				.setOnClickListener(ChangeMonthListener);

		mViewSwitcher = (ViewSwitcher) findViewById(R.id.calendar_switcher);
		mViewSwitcher.addView(getCalendarView());
		mViewSwitcher.addView(getCalendarView());

		mInAnimationLastMonth = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_in);
		mOutAnimationLastMonth = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_out);
		mInAnimationNextMonth = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_in);
		mOutAnimationNextMonth = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_out);
	}

	public void goToDate(Date date) {
		mCurrentMonth.setTime(date);
		// set date to the first day of the month, so we can know which day is the first
		// day of the week.
		mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);

		if (mMoveToNextMonth) {
			mViewSwitcher.setInAnimation(mInAnimationNextMonth);
			mViewSwitcher.setOutAnimation(mOutAnimationNextMonth);
		} else {
			mViewSwitcher.setInAnimation(mInAnimationLastMonth);
			mViewSwitcher.setOutAnimation(mOutAnimationLastMonth);
		}

		mViewSwitcher.showNext();
	}

	private View getCalendarView() {
		TableLayout calendar = new TableLayout(mContext);
		calendar.setStretchAllColumns(true);
		return fillCalendarView(calendar);
	}

	private View fillCalendarView(TableLayout calendar) {
		calendar.removeAllViews();

		int firstDayOfWeek, lastMonthDay, nextMonthDay, currentWeek;

		// get which day is on the first date of the month
		firstDayOfWeek = mCurrentMonth.get(Calendar.DAY_OF_WEEK) - 1;
		currentWeek = mCurrentMonth.get(Calendar.WEEK_OF_YEAR) - 1;

		// adjustment for week number when January starts with first day of month as Sunday
		if (firstDayOfWeek == 0 && mCurrentMonth.get(Calendar.MONTH) == Calendar.JANUARY)
			currentWeek = 1;
		if (currentWeek == 0) currentWeek = 52;

		// lastMonth will be used to display last few dates of previous month in the calendar
		Calendar lastMonth = (Calendar) mCurrentMonth.clone();
		lastMonth.add(Calendar.MONTH, -1);

		// get the number of days in the previous month to display last few days of last month
		lastMonthDay = lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDayOfWeek + 1;

		// next month starts with day 1
		nextMonthDay = 1;

		((TextView) mMonthIndicator.findViewById(R.id.month_and_year))
				.setText(mMonths[mCurrentMonth.get(Calendar.MONTH)] + " "
						+ mCurrentMonth.get(Calendar.YEAR));

		TableRow week = new TableRow(mContext);
		TextView date;

		TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.weight = 1;

		// initialize the day counter to 1, it will be used to display the dates of the month
		int day = 1;
		for (int i = 0; i < 6; i++) {
			if (day > mCurrentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) break;
			week = new TableRow(mContext);
			// this loop is used to fill out the days in the i-th row in the calendar
			for (int j = 0; j < 7; j++) {
				date = new TextView(mContext);
				date.setLayoutParams(lp);
				date.setBackgroundColor(Color.parseColor("#333333"));
				date.setGravity(Gravity.CENTER);
				date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				date.setTextColor(Color.parseColor("#535353"));
				date.setTypeface(null, Typeface.BOLD);

				// last month
				if (j < firstDayOfWeek && day == 1)
					date.setText(String.valueOf(lastMonthDay++));
				// next month
				else if (day > mCurrentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) {
					date.setText(String.valueOf(nextMonthDay++));
				} else // this month
				{
					date.setBackgroundResource(R.drawable.background_normal_days);
					mCurrentMonth.set(Calendar.DAY_OF_MONTH, day);
					date.setOnClickListener(dayClickedListener);

					// today
					if (isToday(mCurrentMonth)) {
						mPreviousSelectedDate = date;
						date.setBackgroundResource(R.drawable.background_today);
					}
					// date selected
					else if (mDateSelected.get(Calendar.MONTH) == mCurrentMonth.get(Calendar.MONTH)
							&& mDateSelected.get(Calendar.DAY_OF_MONTH) == day) {
						mPreviousSelectedDate = date;
						date.setBackgroundResource(R.drawable.background_day_selected);
					}

					date.setText(String.valueOf(day++));

					if (j == 0) // Sunday
						date.setTextColor(Color.parseColor("#D73C10"));
					else if (j == 6) // Saturday
						date.setTextColor(Color.parseColor("#009EF7"));
					else
						date.setTextColor(Color.WHITE);
				}
				date.setPadding(0, 8, 0, 8);
				week.addView(date);
			}
			calendar.addView(week);
		}

		return calendar;
	}

	private boolean isToday(Calendar date) {
		Calendar today = Calendar.getInstance();
		return date.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
				&& date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
	}

	private OnClickListener ChangeMonthListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			ImageButton tv = (ImageButton) view;
			if (tv.getTag().equals("last_month")) {
				mCurrentMonth.add(Calendar.MONTH, -1);
				mMoveToNextMonth = false;
			} else {
				mCurrentMonth.add(Calendar.MONTH, 1);
				mMoveToNextMonth = true;
			}
			TableLayout calendar = (TableLayout) mViewSwitcher.getNextView();
			fillCalendarView(calendar);
			goToDate(mCurrentMonth.getTime());
		}
	};

	private OnClickListener dayClickedListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			view.setBackgroundResource(R.drawable.background_day_selected);
			view.setPadding(0, 8, 0, 8);
			if (mPreviousSelectedDate != null) {
				try {
					mPreviousSelectedDate.setBackgroundResource(R.drawable.background_normal_days);
				} catch (Exception ex) {
					mPreviousSelectedDate.setBackgroundResource(R.drawable.background_normal_days);
				}
			}
			mPreviousSelectedDate.setPadding(0, 8, 0, 8);

			int selectedDay = Integer.parseInt(((TextView) view).getText().toString());
			mDateSelected.set(Calendar.MONTH, mCurrentMonth.get(Calendar.MONTH));
			mDateSelected.set(Calendar.DAY_OF_MONTH, selectedDay);
			mPreviousSelectedDate = (TextView) view;
			
			if (mDateSelectedListener != null)
				mDateSelectedListener.onDateSelected(mDateSelected);
		}
	};
}