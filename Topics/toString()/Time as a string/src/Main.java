class Time {

    private int hours;
    private int minutes;
    private int seconds;

    public Time(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        String timeString = "";
        if (hours < 10) {
            timeString += "0" + hours;
        } else timeString += hours;

        timeString += ":";

        if (minutes < 10) {
            timeString += "0" + minutes;
        } else timeString += minutes;

        timeString += ":";

        if (seconds < 10) {
            timeString += "0" + seconds;
        } else timeString += seconds;

        return timeString;
    }
}