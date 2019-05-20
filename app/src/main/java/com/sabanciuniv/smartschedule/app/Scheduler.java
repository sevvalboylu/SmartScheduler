package com.sabanciuniv.smartschedule.app;

import android.app.Activity;

import com.yandex.mapkit.geometry.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Scheduler extends Activity {

    private boolean scheduledAll = false;

    private String scheduleEnd;
    private String date_str, time_str;
    private Integer listSize;
    private Task.Location location;

    private TaskAdapter adapter = MainActivity.getAdapter(); //get adapter from MainActivity that contains checked Task list
    private ArrayList<Task> freeTasks = new ArrayList<>(); //selected unscheduled tasks
    private ArrayList<Task> schedTasks = new ArrayList<>(); //selected scheduled tasks
    private ArrayList<ViewSchedule.distanceMatrix> distMatrix = new ArrayList<>(); //global distance matrix (filled by bingmaps)

    private HashMap<Integer, Integer> distOrder = new HashMap<>(); //keeps distance matrix
    private HashMap<String, Integer> match = new HashMap<>(); //keeps the matched pairs of tid and tasks's order in list
    private PriorityQueue<Integer> minHeap = new PriorityQueue<>(2 * adapter.checkedTasks.size(), new Comparator<Integer>() { //keeps the min distance
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    });

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

    public Scheduler(Task.Location loc, String scheduleEnd) {
        Calendar calendar = Calendar.getInstance();
        DateFormat dt = new SimpleDateFormat("H:mm:ss");
        DateFormat dd = new SimpleDateFormat("yyyy-MM-dd");
        time_str = dt.format(calendar.getTime());
        date_str = dd.format(calendar.getTime());
        this.location = loc;
        this.scheduleEnd = scheduleEnd;
    }

    public Task getTaskById(String tid) {
        for (Task t : adapter.checkedTasks) {
            if (t.getTid().equals(tid)) return t;
        }
        return null;
    }

    public boolean isScheduledAll()
    {
        return scheduledAll;
    }
    public ArrayList<Task> sortTasks(ArrayList<ViewSchedule.distanceMatrix> dm, String scheduleEnd){
        listSize = adapter.checkedTasks.size();
        distMatrix = dm;

        for (Task t : adapter.checkedTasks)
            if (t.getStartTime() == null && t.getEndTime() == null) freeTasks.add(t);
        for (Task t : adapter.checkedTasks)
            if (t.getStartTime() != null && t.getEndTime() != null) schedTasks.add(t);

        Collections.sort(schedTasks, TaskComparator);

        if(schedTasks.size()>1){
            ArrayList<HashMap.Entry> temp = matchTasks();

            if(temp.size()>0){
                int lastIndex = 0;
                int max = (int) temp.get(temp.size()-1).getValue();

                for(int i =0; i<= max; i++){
                    // candidate list for interval i - i+1
                    ArrayList<HashMap.Entry> candidates = new ArrayList<>();
                    int j;
                    for(j = lastIndex; j< temp.size(); j++) {
                        if((int) temp.get(j).getValue() == i)
                            candidates.add(temp.get(j));
                    }
                    lastIndex = j;

                    // Retrieve the task list using their ID's
                    ArrayList<Task> candTasks = new ArrayList<>();
                    for(HashMap.Entry e: candidates){
                        Task tmp = getTaskById(e.getKey().toString());
                        candTasks.add(tmp);
                    }

                    Collections.sort(candTasks, (task1, task2) -> {
                        return -1 * task1.getLvl().compareTo(task2.getLvl());
                    });
                    if(candTasks.size()>0)
                        attachTasks(i, schedTasks.get(i), schedTasks.get(i + 1), candTasks);

                }

            }
        }
        else if(schedTasks.size() == 1){
            // Insert a dummy task with current time (15 min duration) in current location
            Point p = location.coordinate;
            Task dummy = new Task();
            dummy.setLocation(location);
            String startTime = date_str + "T" + time_str;
            String endTime = timeFormatter(Integer.parseInt(time_str.split(":")[0]),
                    Integer.parseInt(time_str.split(":")[1]) + 15);
            endTime = date_str + "T" + endTime;
            dummy.setStartTime(startTime);
            dummy.setEndTime(endTime);
            schedTasks.add(0, dummy);

            List<HashMap.Entry> temp = matchTasks(); //temp keeps the candidate tasks

            if(temp.size()>0)
            {
                int lastIndex = 0;
                int max = (int) temp.get(temp.size() - 1).getValue();

                for(int i=0; i<= max; i++)
                {
                    ArrayList<HashMap.Entry> candidates = new ArrayList<>();
                    int j;
                    for (j = lastIndex; j < temp.size(); j++) {
                        if ((Integer) temp.get(j).getValue() == i) candidates.add(temp.get(j));
                        else break;
                    } //add all candidates for one interval
                    lastIndex = j;

                    ArrayList<Task> candTasks = new ArrayList<>();
                    for (HashMap.Entry e : candidates) {
                        Task tmp = getTaskById(e.getKey().toString());
                        candTasks.add(tmp);
                    }

                    Collections.sort(candTasks, (task1, task2) -> {
                        return -1 * task1.getLvl().compareTo(task2.getLvl());
                    });

                    if (candTasks.size() > 0)
                        attachTasks(i, schedTasks.get(i), schedTasks.get(i + 1), candTasks);
                }
            }
        }
        else
        {
            // Insert a dummy task with current time (15 min duration) in current location
            Point p = location.coordinate;
            Task dummy = new Task();
            dummy.setLocation(location);
            String startTime = date_str + "T" + time_str;
            String endTime = timeFormatter(Integer.parseInt(time_str.split(":")[0]),
                    Integer.parseInt(time_str.split(":")[1]) + 15);
            endTime = date_str + "T" + endTime;
            dummy.setStartTime(startTime);
            dummy.setEndTime(endTime);
            schedTasks.add(0, dummy);

            ArrayList<mixedArray> mArray = new ArrayList<>();
            for (Task t : freeTasks) {
                double d = getDistMins( dummy.getTid(), t.getTid());
                mixedArray m = new mixedArray(d, t.getLvl(), t.getTid());
                mArray.add(m);
            }

            Collections.sort(mArray, (mixedArray m1, mixedArray m2)-> {
                if (Integer.parseInt(m1.importance) > Integer.parseInt(m2.importance))
                    if (m1.distance < m2.distance) return 1;
                    else if (m1.distance == m2.distance) return 0;
                    else return -1;
                else return -1;
            });


            String tid = mArray.get(mArray.size() - 1).tid;
            Task tmp = getTaskById(tid);
            String start = timeFormatter(Integer.parseInt(scheduleEnd.split(":")[0]),
                    Integer.parseInt(scheduleEnd.split(":")[1])-tmp.getDuration());

            freeTasks.remove(tmp);
            tmp.addRange(start, scheduleEnd);   // todo: this might be a problem in the end part where we schedule remaining free tasks
            schedTasks.add(tmp);

            attachTasks(0, schedTasks.get(0), schedTasks.get(1), freeTasks);
        }

        // todo: do we really need this now??? IDK!!
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
            scheduledAll= true;
        return schedTasks;
    }

    public void attachTasks(int index, Task t1, Task t2, ArrayList<Task> candidates) {
        if (candidates.size() == 0)
            return;

        PriorityQueue<Double> minHeap = new PriorityQueue<>();
        HashMap<Double, String> distOrder = new HashMap<>();

        for (Task t : candidates) { //TODO: middle distance thing is not so optimal I guess.
            double d = findLeastDriving(t1, t2, t);
            minHeap.add(d);
            distOrder.put(d, t.getTid());
        }

        String tid = distOrder.get(minHeap.peek());
        Task tmp = getTaskById(tid);

        ArrayList<String> timerange1 = null, timerange2 = null;
        int hr = 0, min = 0, hr2 = 0, min2 = 0;

        if (t1.getStartTime() == null)
            timerange1 = t1.getRange();
        else {
            hr = Integer.parseInt(t1.getEndHour());
            min = Integer.parseInt(t1.getEndMinute());
        }
        if (t2.getStartTime() == null)
            timerange2 = t2.getRange();
        else {
            hr2 = Integer.parseInt(t2.getStartHour());
            min2 = Integer.parseInt(t2.getStartMinute());
        }

        int driving1 = getDistMins(t1.getTid(), tmp.getTid());
        int driving2 = getDistMins(tmp.getTid(), t2.getTid());
        /*
         * TODO: time ranges are now tight schedules(start+duration = end)
         *       as if they are scheduled. We schedule the nearest the first
         *       then go on accordingly.
         */

        // both t1 and t2 are free tasks.
        if (timerange1 != null && timerange2 != null) {
            String time1 = timeFormatter(Integer.parseInt(timerange1.get(0).split(":")[0]),
                    Integer.parseInt(timerange1.get(0).split(":")[1]) + tmp.getDuration() + driving1);

            String time2 = timeFormatter(Integer.parseInt(timerange2.get(1).split(":")[0]),
                    Integer.parseInt(timerange2.get(1).split(":")[1]) - driving2);

            if (btimeComparator(time1, time2)) {
                String start = timeFormatter(Integer.parseInt(timerange1.get(0).split(":")[0]),
                        Integer.parseInt(timerange1.get(0).split(":")[1]) + driving1);

                String end = time1;

                freeTasks.remove(tmp);
                tmp.addRange(start, end);
                schedTasks.add(index + 1, tmp);
            }
            candidates.remove(tmp);
        }
        // t1 is free task, t2 is scheduled.
        else if (timerange1 != null) {
            String time1 = timeFormatter(Integer.parseInt(timerange1.get(1).split(":")[0]),
                    Integer.parseInt(timerange1.get(1).split(":")[1]) + driving1 + driving2 + tmp.getDuration());
            String time2 = timeFormatter(hr2, min2);

            if (btimeComparator(time1, time2)) {
                String start = timeFormatter(Integer.parseInt(timerange1.get(1).split(":")[0]),
                        Integer.parseInt(timerange1.get(1).split(":")[1]) + driving1);
                String end = timeFormatter(Integer.parseInt(timerange1.get(1).split(":")[0]),
                        Integer.parseInt(timerange1.get(1).split(":")[1]) + driving1 + tmp.getDuration());

                freeTasks.remove(tmp);
                tmp.addRange(start, end);
                schedTasks.add(index + 1, tmp);
            }
            candidates.remove(tmp);
        }
        //t1 is scheduled, t2 is a free task
        else if (timerange2 != null) {
            String time1 = timeFormatter(hr, min + driving1 + driving2 + tmp.getDuration());
            String time2 = timeFormatter(Integer.parseInt(timerange2.get(1).split(":")[0]),
                    Integer.parseInt(timerange2.get(1).split(":")[1]));

            // If previous task's end time + driving + new task's duration is
            // less than the next task's start time, schedule it.
            if (btimeComparator(time1, time2)) {
                String start = timeFormatter(hr, min + driving1);
                String end = timeFormatter(hr, min + driving1 + tmp.getDuration());

                freeTasks.remove(tmp);
                tmp.addRange(start, end);
                schedTasks.add(index + 1, tmp);
            }
            candidates.remove(tmp);
        }
        // both t1 and t2 are scheduled tasks.
        else {
            String time1 = timeFormatter(hr, min + driving1 + tmp.getDuration());
            String time2 = timeFormatter(hr2, min2 - driving2);

            if (btimeComparator(time1, time2)) {
                String start = timeFormatter(hr, min + driving1);
                String end = time2;

                freeTasks.remove(tmp);
                tmp.addRange(start, end);
                schedTasks.add(index + 1, tmp);
            }
            candidates.remove(tmp);
        }

        if (candidates.size() > 0) {
            if (schedTasks.size() > index + 1)
                attachTasks(index, schedTasks.get(index), schedTasks.get(index + 1), candidates); //recursive call to next interval
            if (schedTasks.size() > index + 2)
                attachTasks(index + 1, schedTasks.get(index + 1), schedTasks.get(index + 2), candidates);
        }
    }

    public ArrayList<HashMap.Entry> matchTasks(){

        distOrder.clear(); minHeap.clear(); match.clear(); //clear data containers (else they remain as used before)

        for(Task free: freeTasks){
            int i = 0;
            Task prev = schedTasks.get(i);
            //Task to add(free) is between i and i+1st tasks
            // get the closest driving time.

            while (i<schedTasks.size()){
                if((i+1)!= schedTasks.size()){
                    Task next = schedTasks.get(i+1);
                    int driving = findLeastDriving(prev,next,free);
                    minHeap.add(driving);
                    distOrder.put(driving, i);
                }
                i++;
            }
            if(minHeap.size()>0) {
                int index = minHeap.peek(); // least element in the minHeap
                int order = distOrder.get(index);
                int hour = Integer.parseInt(schedTasks.get(order).getEndHour());
                int min = Integer.parseInt(schedTasks.get(order).getEndMinute());

                // check if the free task's ending time -if we were to schedule it
                // in this interval- would come before next task's start time.
                min += free.getDuration() + getDistMins(free.getTid(), schedTasks.get(order).getTid()) + getDistMins(free.getTid(), schedTasks.get(order + 1).getTid());
                String endFree = timeFormatter(hour, min);
                String startNext = schedTasks.get(order + 1).getStartHour() + ":" + schedTasks.get(order + 1).getStartMinute();
                if (btimeComparator(endFree, startNext))
                    match.put(free.getTid(), order);
            }
        }

        ArrayList<HashMap.Entry> temp = new ArrayList<>(match.entrySet());

        Collections.sort(temp, (entry1, entry2) -> {
            Integer i = (Integer) entry1.getValue();
            Integer j = (Integer) entry2.getValue();
            return i.compareTo(j);
        });

        return temp;
    }

    private int findLeastDriving(Task prev, Task next, Task free) {
        int distPrev = getDistMins(prev.getTid(),free.getTid());
        int distNext = getDistMins(next.getTid(), free.getTid());
        if(distPrev < distNext)
            return distPrev;
        return distNext;
    }

    private int getDistMins(String id1, String id2) {
        for (ViewSchedule.distanceMatrix d:distMatrix) {
            if(d.tid1.equals(id1) && d.tid2.equals(id2))
                return d.duration;
        }
        return 0;
    }

    public static Comparator<Task> TaskComparator = new Comparator<Task>() { //compares tasks by start time
        @Override
        public int compare(Task t1, Task t2) {
            return t1.getStartTime().compareTo(t2.getStartTime());
        }
    };

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
}