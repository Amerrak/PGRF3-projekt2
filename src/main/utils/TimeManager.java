package main.utils;

public class TimeManager {

    public static float deltaTime;

    private static float timeStart = System.nanoTime();
    private static float beginTime;
    private static float endTime;

    public static void init(){
        beginTime = getTime();
        endTime = getTime();
    }

    //Returns time elapsed from timeStart of the application (in seconds)
    public static float getTime(){
        return (float)((System.nanoTime() - timeStart) * 1E-9);
    }

    public static void calculateDeltaTime(){
        endTime = TimeManager.getTime();
        deltaTime = endTime - beginTime;
        beginTime = endTime;
    }
}
