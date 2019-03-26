import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.sabanciuniv.smartschedule.app.MapKitRouteActivity;
import com.sabanciuniv.smartschedule.app.Scheduler;
import com.sabanciuniv.smartschedule.app.Task;
import com.tomtom.online.sdk.common.location.LatLng;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class UnitTest1{
    public ActivityTestRule<Scheduler> activityRule;
    @BeforeClass
    private void launchActivity() {
        //ActivityScenario<MapKitRouteActivity> activityScenario = ActivityScenario.launch(MapKitRouteActivity.class);
        PowerMockito.mockStatic(Log.class);
        activityRule = new ActivityTestRule(Scheduler.class, true, false);
        //we need to create a mock object to send to the functions of class
        //the mock objects are below arrays in the functions.
        //MapKitRouteActivity.getDrivingMins();
    }



    @Test
    public void testSortTasks() throws Throwable {
        //all unscheduled 3 tasks
        Task.Location l1= new Task.Location("Kurna Mahallesi, Unnamed Road, 34916 Pendik/İstanbul, Türkiye", new LatLng(40.9388,29.3519));
        Task.Location l2 = new Task.Location("Cumhuriyet Mahallesi, Sakıp Sabancı Cd. No:95, 34186 Bahçelievler/İstanbul, Türkiye",new LatLng(41.01535163620698,28.852679113874338));
        Task.Location l3 = new Task.Location("Rumelifeneri Mahallesi, Unnamed Road, 34450 Sarıyer/İstanbul, Türkiye",new LatLng(41.2048,29.0718));
        Task.Location l4 = new Task.Location("Orta Mahallesi, Diller Okulu, 34956 Tuzla/İstanbul, Türkiye",new LatLng(40.891715,29.377708));
        Task.Location l5 = new Task.Location("Ballıca Mahallesi, Unnamed Road, 34916 Pendik/İstanbul, Türkiye",new LatLng(40.991284596065235,29.393384374241467));

        Task t1 = new Task("test","unsched1","3",10,"",l1);
        Task t2 = new Task("test","unsched2","2",5,"",l2);
        Task t3 = new Task("test","unsched3","1",15,"",l3);
        Task t4 = new Task("test","unsched3","2",20,"",l4);
        Task t5 = new Task("test","unsched3","3",5,"",l5);

        // Task(String uid, String tid, String title, Location location, int duration, String lvl, String startTime, String endTime)
        Task t6 = new Task("test", "sched1", "", l5,30,"2","2019-04-23T20:30:29.823+03:00", "2019-04-23T21:00:29.823+03:00");
        //t7 and t8 are on same day
        Task t7 = new Task("test", "sched2", "", l4,60,"1", "2019-04-17T19:00:54.062+03:00", "2019-04-17T20:00:54.062+03:00");
        Task t8 = new Task("test", "sched3", "", l4,90,"1", "2019-04-17T13:00:54.062+03:00", "2019-04-17T14:30:54.062+03:00");
        Task t9 = new Task("test", "sched4", "", l4,120,"1", "2019-04-27T15:00:54.062+03:00", "2019-04-27T17:00:54.062+03:00");
        Task t10 = new Task("test", "sched5", "", l4,120,"3", "2019-04-27T11:00:54.062+03:00", "2019-04-27T12:00:54.062+03:00");

        List<Task> allUnsched_3 = new ArrayList<Task>();
        allUnsched_3.add(t1);
        allUnsched_3.add(t2);
        allUnsched_3.add(t3);

        //all unscheduled 5 tasks
        List<Task> allUnsched_5 = new ArrayList<Task>();
        allUnsched_5.add(t1);
        allUnsched_5.add(t2);
        allUnsched_5.add(t3);
        allUnsched_5.add(t4);
        allUnsched_5.add(t5);

        //3 scheduled 2 unscheduled tasks
        //scenario: breakfast in the morning, then shop in mall afternoon, movies in evening
        List<Task> unsched3_sched2 = new ArrayList<Task>();
        unsched3_sched2.add(t1);
        unsched3_sched2.add(t2);
        unsched3_sched2.add(new Task("test", "sched21", "sunday breakfast", l3,90,"3", "2019-04-14T10:00:54.062+03:00", "2019-04-14T11:30:54.062+03:00"));
        unsched3_sched2.add(new Task("test", "sched22", "mall", l4,180,"1", "2019-04-14T14:00:54.062+03:00", "2019-04-14T17:00:54.062+03:00"));
        unsched3_sched2.add(new Task("test", "sched23", "movies", l4,180,"3", "2019-04-14T19:00:54.062+03:00", "2019-04-14T22:00:54.062+03:00"));

        //2 scheduled 3 unscheduled tasks
        List<Task> unsched2_sched3 = new ArrayList<Task>();
        unsched2_sched3.add(t1);
        unsched2_sched3.add(t2);
        unsched2_sched3.add(t3);
        unsched2_sched3.add(t7);
        unsched2_sched3.add(t8);

        //4 unscheduled 1 scheduled tasks
        List<Task> unsched4_sched1 = new ArrayList<Task>();
        unsched4_sched1.add(t1);
        unsched4_sched1.add(t2);
        unsched4_sched1.add(t3);
        unsched4_sched1.add(t4);
        unsched4_sched1.add(t6);

        //1 unscheduled 4 scheduled tasks, all same day
        //scenario: meeting in morning, lunch with colleague, gym late afternoon, dinner w/partner at evening
        List<Task> unsched1_sched4 = new ArrayList<Task>();
        unsched1_sched4.add(t1);
        unsched1_sched4.add(new Task("test", "sched41", "meeting", l4,120,"3", "2019-04-27T09:00:54.062+03:00", "2019-04-27T11:00:54.062+03:00"));
        unsched1_sched4.add(new Task("test", "sched42", "lunch", l4,60,"2", "2019-04-27T12:00:54.062+03:00", "2019-04-27T13:00:54.062+03:00"));
        unsched1_sched4.add(new Task("test", "sched43", "gym", l3,70,"3", "2019-04-27T16:50:54.062+03:00", "2019-04-27T18:00:54.062+03:00"));
        unsched1_sched4.add(new Task("test", "sched44", "dinner", l2,90,"2", "2019-04-27T20:00:54.062+03:00", "2019-04-27T21:30:54.062+03:00"));


        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                activityRule.getActivity().getDrivingMins();
                //bu task listesi, bu driving datası, bu sonuç
                //sorttask çalışıyor mu buna bakalım


                //firebasete oluştur. breakpoint oluştur ve datayı manuel olarak gir.
                //
                //activityRule.getActivity().sortTasks(dm);
            }
        });
    }
    @After
    private void unregisterIdlingResource() {

    }
}