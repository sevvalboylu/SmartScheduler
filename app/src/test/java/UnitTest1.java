import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.sabanciuniv.smartschedule.app.MapKitRouteActivity;
import com.sabanciuniv.smartschedule.app.Task;
import com.tomtom.online.sdk.common.location.LatLng;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class UnitTest1{ public ActivityTestRule<MapKitRouteActivity> activityRule;
    @BeforeClass
    private void launchActivity() {
        //ActivityScenario<MapKitRouteActivity> activityScenario = ActivityScenario.launch(MapKitRouteActivity.class);
        PowerMockito.mockStatic(Log.class);
        activityRule = new ActivityTestRule(MapKitRouteActivity.class, true, false);
    }

    @Test
    public void testSortTasks() throws Throwable {
        //all unscheduled 3 tasks
        Task.Location l1= new Task.Location("Kurna Mahallesi, Unnamed Road, 34916 Pendik/İstanbul, Türkiye", new LatLng(40.9388,29.3519));
        Task.Location l2 = new Task.Location("Cumhuriyet Mahallesi, Sakıp Sabancı Cd. No:95, 34186 Bahçelievler/İstanbul, Türkiye",new LatLng(41.01535163620698,28.852679113874338));
        Task.Location l3 = new Task.Location("Rumelifeneri Mahallesi, Unnamed Road, 34450 Sarıyer/İstanbul, Türkiye",new LatLng(41.2048,29.0718));

        Task t1 = new Task("test","unsched1","3",10,"",l1);
        Task t2 =  new Task("test","unsched2","2",5,"",l2);
        Task t3 = new Task("test","unsched3","1",10,"",l3);


        ArrayList<Task> allUnsched_3 = new ArrayList<Task>();
        allUnsched_3.add(t1);
        allUnsched_3.add(t2);
        allUnsched_3.add(t3);

        //all unscheduled 5 tasks
        Task.Location l4 = new Task.Location("Orta Mahallesi, Diller Okulu, 34956 Tuzla/İstanbul, Türkiye",new LatLng(40.891715,29.377708));
        Task.Location l5 = new Task.Location("Ballıca Mahallesi, Unnamed Road, 34916 Pendik/İstanbul, Türkiye",new LatLng(40.991284596065235,29.393384374241467));

        ArrayList<Task> allUnsched_5 = new ArrayList<Task>();
        allUnsched_5.add(t1);
        allUnsched_5.add(t2);
        allUnsched_5.add(new Task("test","unsched3","1",15,"",l3));
        allUnsched_5.add(new Task("test","unsched3","2",20,"",l4));
        allUnsched_5.add(new Task("test","unsched3","3",5,"",l5));

        //3 scheduled 2 unscheduled tasks
        ArrayList<Task> unsched3_sched2 = new ArrayList<Task>();
        unsched3_sched2.add(t1);
        unsched3_sched2.add(t2);


        //2 scheduled 3 unscheduled tasks
        ArrayList<Task> unsched2_sched3 = new ArrayList<Task>();

        //4 unscheduled 1 scheduled tasks
        ArrayList<Task> unsched4_sched1 = new ArrayList<Task>();

        //1 unscheduled 4 scheduled tasks
        ArrayList<Task> unsched1_sched4 = new ArrayList<Task>();

        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().getDrivingMins();
            }
        });
    }
    @After
    private void unregisterIdlingResource() {

    }
}