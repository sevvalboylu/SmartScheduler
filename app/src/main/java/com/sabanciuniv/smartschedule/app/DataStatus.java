package com.sabanciuniv.smartschedule.app;

import java.util.ArrayList;
import java.util.List;

public interface DataStatus {
        void TasksLoaded(List<Task> tasks, List<String> keys);
        void LocsLoaded(ArrayList<Profile.Location> locs, List<String> keys);
    }

