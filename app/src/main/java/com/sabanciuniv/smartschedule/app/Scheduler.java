package com.sabanciuniv.smartschedule.app;

import android.app.Activity;
import android.os.Build;

import com.yandex.mapkit.geometry.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Scheduler extends Activity {

    private String scheduleEnd;
    private ArrayList<Task> freeTasks = new ArrayList<>(); //selected unscheduled tasks
    private ArrayList<Task> schedTasks = new ArrayList<>(); //selected scheduled tasks
    private Task.Location location; //configuration taken from main act.(chosen tasks)
    private boolean isAllDone = false;

    private int listSize;
    private String time_str;
    private String date_str;

    public boolean scheduledAll(){
        return isAllDone;
    }
    TaskAdapter adapter = MainActivity.getAdapter();

    private class mixedArray {
        double distance;
        String importance;
        String tid;

        protected mixedArray(double distance, String importance, String tid) {
            this.distance = distance;
            this.importance = importance;
            this.tid = tid;
        }
    }
  
    HashMap<Double, Integer> distOrder = new HashMap<>(); //keeps distance matrix
    HashMap<String, Integer> match = new HashMap<>(); //keeps the matched pairs of tid and tasks's order in list
    ArrayList<ViewSchedule.distanceMatrix> dmGlobal = new ArrayList<>(); //global distance matrix (filled by bingmaps)
    PriorityQueue<Double> minHeap = new PriorityQueue<>(2 * adapter.checkedTasks.size(), new Comparator<Double>() { //keeps the min distance
        @Override
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    });


    public List<Map.Entry> matchTasks() {

        distOrder.clear();
        minHeap.clear();
        match.clear();

        for (Task fr : freeTasks) {
            Integer i = 0;

            while (i < schedTasks.size()) {
                Task t = schedTasks.get(i);

                //task is between ith and i+1th if it exists
                //get midpoint and get distance
                if ((i + 1) != schedTasks.size()) {
                    Task p = schedTasks.get(i + 1);
                    double d = findDistMid(t.getLocation().coordinate, p.getLocation().coordinate, fr.getLocation().coordinate);
                    minHeap.add(d);
                    distOrder.put(d, i);
                }

                i++;
            }

            Double index = minHeap.peek();
            int order = distOrder.get(index);
            int hr = Integer.parseInt(schedTasks.get(order).getEndHour());
            int min = Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getEndMinute());
            min += fr.getDuration() + getDistMins(fr.getTid(), schedTasks.get(distOrder.get(minHeap.peek()) + 1).getTid());
            String s = timeFormatter(hr, min);
            String s1 = schedTasks.get(distOrder.get(minHeap.peek()) + 1).getStartHour() + ":" + schedTasks.get(distOrder.get(minHeap.peek()) + 1).getStartMinute();
            if (btimeComparator(s, s1))
                match.put(fr.getTid(), distOrder.get(minHeap.peek())); //candidate unscheduled tasks to be inserted between ith and i+1th task

        }
        //sort HashMap by value

        List<HashMap.Entry> temp = new LinkedList<>(match.entrySet());

        Collections.sort(temp, new Comparator<HashMap.Entry>() {
            @Override
            public int compare(Map.Entry entry, Map.Entry t1) {
                Integer i = (Integer) entry.getValue();
                Integer j = (Integer) t1.getValue();
                return i.compareTo(j);
            }

        });

        return temp;
    }

    public Task getTaskById(String tid) {
        for (Task t : adapter.checkedTasks) {
            if (t.getTid().equals(tid)) return t;
        }
        return null;
    }


    public Scheduler(Task.Location loc, String scheduleEnd) {
        Calendar cal = Calendar.getInstance(); //today - now
        DateFormat dt = new SimpleDateFormat("H:mm:ss");
        DateFormat dd = new SimpleDateFormat("yyyy-MM-dd");
        time_str = dt.format(cal.getTime());
        date_str = dd.format(cal.getTime());
        this.location = loc;
        this.scheduleEnd = scheduleEnd;
    }

    public ArrayList<Task> sortTasks(ArrayList<ViewSchedule.distanceMatrix> dm, String scheduleEnd) {
        //tasks with given time are already assigned
        //others can not overlap
        //eliminate the ones with fixed slot
        dmGlobal = dm;
        listSize = adapter.checkedTasks.size();

        for (Task t : adapter.checkedTasks)
            if (t.getStartTime() == null && t.getEndTime() == null) freeTasks.add(t);
        for (Task t : adapter.checkedTasks)
            if (t.getStartTime() != null && t.getEndTime() != null) schedTasks.add(t);

        Collections.sort(schedTasks, TaskComparator);

        if (schedTasks.size() > 1) { //TODO: NOT OPTIMAL SCHEDULE WITH 2 SCHEDULED TASKS
            List<HashMap.Entry> temp = matchTasks();
            //now evaluate the candidate tasks in match

            if (temp.size() > 0) {
                int lastindex = 0;
                Integer max = (Integer) temp.get(temp.size() - 1).getValue();
                for (int k = 0; k <= max; k++) {
                    List<HashMap.Entry> candidates = new LinkedList<>();
                    int j;
                    for (j = lastindex; j < temp.size(); j++) {
                        if ((Integer) temp.get(j).getValue() == k) candidates.add(temp.get(j));
                        else break;
                    } //add all candidates for one interval
                    lastindex = j;

                    List<Task> candTasks = new LinkedList<>();
                    for (HashMap.Entry e : candidates) {
                        Task tmp = getTaskById(e.getKey().toString());
                        candTasks.add(tmp);
                    }

                    //now one by one evaluate the candidate tasks
                    Collections.sort(candTasks, new Comparator<Task>() {
                        @Override
                        public int compare(Task task, Task t1) {
                            return -1 * task.getLvl().compareTo(t1.getLvl());
                        }
                    });

                    if (candTasks.size() > 0)
                        attachTasks(k, schedTasks.get(k), schedTasks.get(k + 1), candTasks);

                }
            }

        }
        else if (schedTasks.size() == 1) {

            Point p = location.coordinate;
            ArrayList<mixedArray> mArray = new ArrayList<>();
            for (Task t : freeTasks) {
                double d = findDist(p, t.getLocation().coordinate);
                mixedArray m = new mixedArray(d, t.getLvl(), t.getTid());
                mArray.add(m);
            }

            Collections.sort(mArray, new Comparator<mixedArray>() {
                @Override
                public int compare(mixedArray m1, mixedArray m2) {
                    if (Integer.parseInt(m1.importance) > Integer.parseInt(m2.importance)) {
                        if (m1.distance < m2.distance) return 1;
                        else if (m1.distance == m2.distance) return 0;
                        else return -1;
                    } else return -1;
                }
            });

            Task dummy = new Task();
            dummy.setLocation(location);

            String startTime = date_str + "T" + time_str;
            String endTime = timeFormatter(Integer.parseInt(time_str.split(":")[0]), Integer.parseInt(time_str.split(":")[1]) + 30);
            endTime = date_str + "T" + endTime;
            dummy.setStartTime(startTime);
            dummy.setEndTime(endTime);
            schedTasks.add(0, dummy);

            List<HashMap.Entry> temp = matchTasks(); //temp keeps the candidate tasks

            if (temp.size() > 0) {
                int lastindex = 0;
                Integer max = (Integer) temp.get(temp.size() - 1).getValue();
                for (int k = 0; k <= max; k++) {
                    List<HashMap.Entry> candidates = new LinkedList<>();
                    int j;
                    for (j = lastindex; j < temp.size(); j++) {
                        if ((Integer) temp.get(j).getValue() == k) candidates.add(temp.get(j));
                        else break;
                    } //add all candidates for one interval
                    lastindex = j;

                    List<Task> candTasks = new LinkedList<>();
                    for (HashMap.Entry e : candidates) {
                        Task tmp = getTaskById(e.getKey().toString());
                        candTasks.add(tmp);
                    }

                    //now one by one evaluate the candidate tasks
                    Collections.sort(candTasks, new Comparator<Task>() {
                        @Override
                        public int compare(Task task, Task t1) {
                            return -1 * task.getLvl().compareTo(t1.getLvl());
                        }
                    });

                    if (candTasks.size() > 0)
                        attachTasks(k, schedTasks.get(k), schedTasks.get(k + 1), candTasks);

                }
            }
        }
        else //all of them are unscheduled, schedule nearest & treat as other case
        {
            // use location = current location
            Point p = location.coordinate;
            ArrayList<mixedArray> mArray = new ArrayList<>();
            for (Task t : freeTasks) {
                double d = findDist(p, t.getLocation().coordinate);
                mixedArray m = new mixedArray(d, t.getLvl(), t.getTid());
                mArray.add(m);
            }

            Collections.sort(mArray, new Comparator<mixedArray>() {
                @Override
                public int compare(mixedArray m1, mixedArray m2) {
                    if (Integer.parseInt(m1.importance) > Integer.parseInt(m2.importance))
                        if (m1.distance < m2.distance) return 1;
                        else if (m1.distance == m2.distance) return 0;
                        else return -1;
                    else return -1;
                }
            });

            String tid = mArray.get(mArray.size() - 1).tid;

            //check if we can attach another task now
            Task dummy = new Task();
            dummy.setLocation(location);
            String startTime = date_str + "T" + time_str;
            String endTime = timeFormatter(Integer.parseInt(time_str.split(":")[0]), Integer.parseInt(time_str.split(":")[1]) + 30);
            endTime = date_str + "T" + endTime;
            dummy.setStartTime(startTime);
            dummy.setEndTime(endTime);
            schedTasks.add(0, dummy);

            String earliestStart = timeFormatter(Integer.parseInt(time_str.split(":")[0]), Integer.parseInt(time_str.split(":")[1]) + getDistMins("0", tid) + 30);
            String latestEnd = timeFormatter(Integer.parseInt(earliestStart.split(":")[0]) + 6, Integer.parseInt(earliestStart.split(":")[1]));
            // todo: update latest end by the time we get from user

            Task tmp = getTaskById(tid);
            freeTasks.remove(tmp);
            tmp.addRange(earliestStart, latestEnd);
            schedTasks.add(tmp);

            attachTasks(0, schedTasks.get(0), schedTasks.get(1), freeTasks);
        }

        //schedule the remaining free tasks
        if(freeTasks.size()>0)
        {
            // Add to the ends (beginning if we have time)
            Task first = schedTasks.get(0);
            Task last = schedTasks.get(schedTasks.size() - 1);

            HashMap<Task, Integer> sortedFree = new HashMap<Task, Integer>();
            //sort free tasks
            for (Task t :freeTasks) {
                int minF = getDistMins(first.getTid(), t.getTid());
                int minL = getDistMins(last.getTid(), t.getTid());
                if(minF<minL)
                    sortedFree.put(t, minF);
                else
                    sortedFree.put(t, minL);
            }

            Collections.sort(freeTasks,new Comparator<Task>() {
                @Override
                public int compare(Task task1, Task task2) {
                    if(sortedFree.get(task1)< sortedFree.get(task2))
                        return -1;
                    else if(sortedFree.get(task1) > sortedFree.get(task2))
                        return 1;
                    return 0;
                }
            });

            for (Task fr : freeTasks) {
                int minF = getDistMins(first.getTid(), fr.getTid());
                int minL = getDistMins(last.getTid(), fr.getTid()); ArrayList<String> timerange1 = null;
                if (minF >= minL) {
                    Task lastTask = schedTasks.get(schedTasks.size()-1);
                    if(lastTask.getRange().size() == 0)
                    {
                        String end = lastTask.getEndTime();
                        end = end.split("T")[1];
                        // endtime + driving + duration < latestend
                        String [] endSplit = end.split(":");
                        if(btimeComparator(timeFormatter(Integer.parseInt(endSplit[0]),
                                Integer.parseInt(endSplit[1])+ minL + fr.getDuration()),scheduleEnd))
                        {
                            String start = timeFormatter(Integer.parseInt(lastTask.getEndHour()),
                                    Integer.parseInt(lastTask.getEndMinute())+minL);
                            String end2 = timeFormatter(Integer.parseInt(start.split(":")[0]),
                                    Integer.parseInt(start.split(":")[1])+ fr.getDuration());
                            fr.addRange(start,end2);
                            schedTasks.add(fr);
                        }
                    }
                    else
                    {
                        String end = lastTask.getRange().get(1);
                        end = timeFormatter(Integer.parseInt(end.split(":")[0])
                                , Integer.parseInt(end.split(":")[1])- lastTask.getDuration());
                        // endtime + driving + duration < latestend
                        String [] endSplit = end.split(":");
                        if(btimeComparator(timeFormatter(Integer.parseInt(endSplit[0]),
                                Integer.parseInt(endSplit[1])+minL + fr.getDuration()),scheduleEnd))
                        {
                            String endSch = lastTask.getRange().get(1);
                            String start = timeFormatter(Integer.parseInt(endSch.split(":")[0]),
                                    Integer.parseInt(endSch.split(":")[1])+minL);
                            String end2 = timeFormatter(Integer.parseInt(endSch.split(":")[0]),
                                    Integer.parseInt((endSch.split(":")[1])) + minL + fr.getDuration());
                            fr.addRange(start,end2);
                            schedTasks.add(fr);
                        }
                    }
                }
                else{
                    if (first.getStartTime() == null)
                        timerange1 = first.getRange();
                    if(timerange1 == null) // first is scheduled
                    {
                        // current time + driving + duration < first's start time, then schedule, otherwise add to end OR don't schedule.
                        String[] index = time_str.split(":");
                        int startHr = Integer.parseInt(index[0]);
                        int startMin = Integer.parseInt(index[1])+ minF + fr.getDuration();
                        String newTime = timeFormatter(startHr,startMin);
                        String starttime = first.getStartTime().split("T")[1];
                        if(btimeComparator(newTime,starttime))
                            schedTasks.add(0, fr);
                        else
                        {
                            Task lastTask = schedTasks.get(schedTasks.size()-1);
                            if(lastTask.getRange().size() == 0)
                            {
                                String end = lastTask.getEndTime();
                                end = end.split("T")[1];
                                // endtime + driving + duration < latestend
                                String [] endSplit = end.split(":");
                                if(btimeComparator(timeFormatter(Integer.parseInt(endSplit[0]),
                                        Integer.parseInt(endSplit[1])+minL + fr.getDuration()),scheduleEnd))
                                {
                                    String start = timeFormatter(Integer.parseInt(lastTask.getEndHour()),
                                            Integer.parseInt(lastTask.getEndMinute())+minL);
                                    String end2 = timeFormatter(Integer.parseInt(start.split(":")[0]),
                                            Integer.parseInt(start.split(":")[1])+ fr.getDuration());
                                    fr.addRange(start,end2);
                                    schedTasks.add(fr);
                                }
                            }
                            else
                            {
                                String end = lastTask.getRange().get(1);
                                end = timeFormatter(Integer.parseInt(end.split(":")[0])
                                        , Integer.parseInt(end.split(":")[1])- lastTask.getDuration());
                                // endtime + driving + duration < latestend
                                String [] endSplit = end.split(":");
                                if(btimeComparator(timeFormatter(Integer.parseInt(endSplit[0]),
                                        Integer.parseInt(endSplit[1])+minL + fr.getDuration()),scheduleEnd))
                                {
                                    String endSch = lastTask.getRange().get(1);
                                    String start = timeFormatter(Integer.parseInt(endSch.split(":")[0]),
                                            Integer.parseInt(endSch.split(":")[1])+minL);
                                    String end2 = timeFormatter(Integer.parseInt(endSch.split(":")[0]),
                                            Integer.parseInt((endSch.split(":")[1])) + minL + fr.getDuration());
                                    fr.addRange(start,end2);
                                    schedTasks.add(fr);
                                }
                            }
                        }
                    }
                    else
                    {
                        int index = time_str.indexOf(":");
                        int startHr = Integer.parseInt(time_str.substring(0, index));
                        int startMin = Integer.parseInt(time_str.substring(index+1))+ minF + fr.getDuration() + first.getDuration();
                        String newTime = timeFormatter(startHr,startMin);
                        String latestEnd = timerange1.get(1);
                        if(btimeComparator(newTime,latestEnd))
                            schedTasks.add(0, fr);
                        else {
                            Task lastTask = schedTasks.get(schedTasks.size()-1);
                            if(lastTask.getRange().size() == 0) //scheduled task
                            {
                                String end = lastTask.getEndTime();
                                end = end.split("T")[1];
                                // endtime + driving + duration < latestend
                                String [] endSplit = end.split(":");
                                if(btimeComparator(timeFormatter(Integer.parseInt(endSplit[0]),
                                        Integer.parseInt(endSplit[1])+minL + fr.getDuration()),scheduleEnd))
                                {
                                    schedTasks.add(fr);
                                }
                            }
                            else
                            {
                                String end = lastTask.getRange().get(1);
                                end = timeFormatter(Integer.parseInt(end.split(":")[0])
                                        , Integer.parseInt(end.split(":")[1])- lastTask.getDuration());
                                // endtime + driving + duration < latestend
                                String [] endSplit = end.split(":");
                                if(btimeComparator(timeFormatter(Integer.parseInt(endSplit[0]),
                                        Integer.parseInt(endSplit[1])+minL + fr.getDuration()),scheduleEnd))
                                {
                                    String endSch = lastTask.getRange().get(1);
                                    String start = timeFormatter(Integer.parseInt(endSch.split(":")[0]),
                                            Integer.parseInt(endSch.split(":")[1])+minL);
                                    String end2 = timeFormatter(Integer.parseInt(endSch.split(":")[0]),
                                            Integer.parseInt((endSch.split(":")[1])) + minL + fr.getDuration());
                                    fr.addRange(start,end2);
                                    schedTasks.add(fr);
                                }
                            }
                        }
                    }
                }
            }

        }

        for (Task t :schedTasks) {
            if(t.getTitle() == null){
                schedTasks.remove(t);
                break;
            }
        }

        if(listSize == schedTasks.size())
            isAllDone = true;
        return schedTasks;
    }

    public void attachTasks(int index, Task t1, Task t2, List<Task> candidateTasks) { //consider candidates
        if (candidateTasks.size() == 0) return;

        PriorityQueue<Double> minHeap = new PriorityQueue<>();
        HashMap<Double, String> distOrder = new HashMap<>();

        for (Task t : candidateTasks) { //TODO: middle distance thing is not so optimal I guess.
            double d = findDistMid(t1.getLocation().coordinate, t2.getLocation().coordinate, t.getLocation().coordinate);
            minHeap.add(d);
            distOrder.put(d, t.getTid());
        }

        String tid = distOrder.get(minHeap.peek());
        Task tmp = getTaskById(tid);

        ArrayList<String> timerange1 = null, timerange2 = null;
        int hr = 0, min = 0, hr2 = 0, min2 = 0;
        if (t1.getStartTime() == null) timerange1 = t1.getRange();
        else {
            hr = Integer.parseInt(t1.getEndHour());
            min = Integer.parseInt(t1.getEndMinute());
        }
        if (t2.getStartTime() == null) timerange2 = t2.getRange();
        else {
            hr2 = Integer.parseInt(t2.getStartHour());
            min2 = Integer.parseInt(t2.getStartMinute());
        }
        int driving = getDistMins(t1.getTid(),t2.getTid());
        if (timerange1 == null && timerange2 != null) {
            //t1 scheduled, t2 free
            String time1 = timeFormatter(hr, min + driving + tmp.getDuration());
            String time2 = timeFormatter(Integer.parseInt(timerange2.get(1).split(":")[0]), Integer.parseInt(timerange2.get(1).split(":")[1]) - t2.getDuration() - driving);
            if (btimeComparator(time1, time2)) {
                String start = timeFormatter(hr, min + driving);
                hr2 = Integer.parseInt(timerange2.get(1).split(":")[0]);
                min2 = Integer.parseInt(timerange2.get(1).split(":")[1]);
                String end = timeFormatter(hr2, min2 - driving - t2.getDuration());
                tmp.addRange(start, end);

                schedTasks.remove(t2);
                t2.addRange(timeFormatter(Integer.parseInt(end.split(":")[0]), Integer.parseInt(end.split(":")[1]) + driving), t2.getRange().get(1));
                schedTasks.add(index + 1, t2);
                schedTasks.add(index + 1, tmp);
                candidateTasks.remove(tmp);
                freeTasks.remove(tmp);
            }
        }
        else if (timerange1 != null && timerange2 == null) {
            //t1 free, t2 scheduled
            //todo: here
            hr = Integer.parseInt(timerange1.get(0).split(":")[0]);
            min = Integer.parseInt(timerange1.get(0).split(":")[1]) + tmp.getDuration() + driving + t1.getDuration();

            if (btimeComparator(timeFormatter(hr, min), timeFormatter(hr2, min2))) {
                String start = timeFormatter(hr, min - tmp.getDuration());
                String end = timeFormatter(hr2, min2 - driving);
                tmp.addRange(start, end);

                schedTasks.remove(t1);
                t1.addRange(t1.getRange().get(0), timeFormatter(Integer.parseInt(tmp.getRange().get(0).split(":")[0]), Integer.parseInt(tmp.getRange().get(0).split(":")[1]) - driving));
                schedTasks.add(index, t1);
                schedTasks.add(index + 1, tmp);
                freeTasks.remove(tmp);
            }
            candidateTasks.remove(tmp);

        } else if (timerange1 != null && timerange2 != null) {
            //t1 free, t2 free
            //todo: here
            hr = Integer.parseInt(timerange1.get(0).split(":")[0]);
            min = Integer.parseInt(timerange1.get(0).split(":")[1]) + tmp.getDuration() + driving + t1.getDuration();

            hr2 = Integer.parseInt(timerange2.get(1).split(":")[0]);
            min2 = Integer.parseInt(timerange2.get(1).split(":")[1]) - t2.getDuration() - driving;

            if (btimeComparator(timeFormatter(hr, min), timeFormatter(hr2, min2))) {
                String start = timeFormatter(hr, min - tmp.getDuration());
                String end = timeFormatter(hr2, min2);
                tmp.addRange(start, end);

                schedTasks.remove(t1);
                t1.addRange(t1.getRange().get(0), timeFormatter(Integer.parseInt(start.split(":")[0]), Integer.parseInt(start.split(":")[1]) - driving));
                schedTasks.add(index, t1);

                schedTasks.remove(t2);
                t2.addRange(timeFormatter(Integer.parseInt(end.split(":")[0]), Integer.parseInt(end.split(":")[1]) + driving), t2.getRange().get(1));
                schedTasks.add(index + 1, t2);

                schedTasks.add(index + 1, tmp);
                freeTasks.remove(tmp);
            }
            candidateTasks.remove(tmp);
        } else {
            //when we have two driving durations, also replace min2 with min2-driving2
            if (btimeComparator(timeFormatter(hr, min + driving + tmp.getDuration()), timeFormatter(hr2, min2 - getDistMins(t2.getTid(),schedTasks.get(index+1).getTid())))) {
                String start = timeFormatter(hr, min + driving);
                String end = timeFormatter(hr2, min2 - driving);
                tmp.addRange(start, end);
                schedTasks.add(index + 1, tmp);
                candidateTasks.remove(tmp);
                freeTasks.remove(tmp);
            }
        }

        if (candidateTasks.size() > 0) {
            if (schedTasks.size() > index + 1)
                attachTasks(index, schedTasks.get(index), schedTasks.get(index + 1), candidateTasks); //recursive call to next interval
            if (schedTasks.size() > index + 2)
                attachTasks(index + 1, schedTasks.get(index + 1), schedTasks.get(index + 2), candidateTasks);
        }

    }

    private boolean btimeComparator(String s, String s1) //returns 1 if left op is sooner
    {
        if (Integer.parseInt(s.split(":")[0]) > Integer.parseInt(s1.split(":")[0])) return false;
        else if (Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if (Integer.parseInt(s.split(":")[1]) < Integer.parseInt(s1.split(":")[1])) return true;
            else return false;
        else return true;

    }

    private String timeFormatter(int hr, int min) {
        int mm1 = min;
        int hr1 = hr;
        if (min > 59) {
            while (mm1 > 59) {
                mm1 = mm1 - 60;
                hr1 += 1;
            }
        }
        if (min < 0) { //todo
            while (mm1 < 0) {
                hr1 -= 1;
                mm1 = 60 + mm1;
            }
        }
        if(mm1<10)
            return String.valueOf(hr1) + ":0" + String.valueOf(mm1);
        return String.valueOf(hr1) + ":" + String.valueOf(mm1);
    }

    public static Comparator<Task> TaskComparator = new Comparator<Task>() { //compares tasks by start time

        @Override
        public int compare(Task t1, Task t2) {
            return t1.getStartTime().compareTo(t2.getStartTime());
        }
    };

    private double findDistMid(Point p1, Point p2, Point p3) // get p3's distance from midpoint of p1 and p2
    {
        Point p = new Point((p1.getLatitude() + p2.getLatitude()) / 2, (p1.getLongitude() + p2.getLongitude()) / 2);
        return findDist(p, p3);
    }

    private double findDist(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.getLatitude() - p2.getLatitude(), 2) + Math.pow(p1.getLongitude() - p2.getLongitude(), 2));
    }


    private int getDistMins(String id1, String id2) {
        for (ViewSchedule.distanceMatrix d:dmGlobal) {
          if(d.tid1.equals(id1) && d.tid2.equals(id2))
              return d.duration;
        }
        return 0;
    }

    public ArrayList<ViewSchedule.distanceMatrix> getDmGlobal() {
        return dmGlobal;
    }
}
