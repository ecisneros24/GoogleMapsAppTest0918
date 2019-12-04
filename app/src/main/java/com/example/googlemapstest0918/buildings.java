package com.example.googlemapstest0918;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

public class buildings extends AppCompatActivity {

    ListView listView;
    String[] buildings = {"College of Education (COE)","Innovation and Instruction(II)","Leo Cain Library (LIB)",
            "James l. Welch Hall (WH)", "Student Health Center (SHC)", "Loker Student Union (LSU)", "Social and Behavioral Sciences (SBS)",
            "Lacorte Hall (LCH)", "University Theater (UT)", "Natural Sciences and Mathmatics (NSM)","Science and Innovation (SI)",
            "Gymnasium (Gym)", "Field House (FH)", "Swimming Pool (SP)", "ROTC & Parking Services Modular (RPM)",
            "South Academy Complex 2 (SAC-2)", "South Academy Complex 3 (SAC-3)"
    };
    String[] abriv ={"COE", "II", "LIB","WH", "SHC","LSU","SBS","LCH","UT","NSM","SI","GYM","FH","SP","RPM","SAC-2",
            "SAC-3"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildings);
        //return super.onCreateOptionsMenu();
    }
}
 ////////This isn't doing anything, it was an attempt to make things easier that failed