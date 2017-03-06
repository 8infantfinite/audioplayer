package ru.startandroid.musicplayer;


public class ConvertTime {
	
	/**
	 * Метод для того, чтобы перевести миллисекунды
	 * в обычный формат времени Часы:Минуты:Секунды
	 * */
	public String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";
		
		// Перевод продолжительности песни из милисекунд в обычный формат времени
		   int hours = (int)( milliseconds / (1000*60*60));
		   int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		   int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		   // Если есть часы, добавляем
		   if(hours > 0){
			   finalTimerString = hours + ":";
		   }
		   
		   // добавляем первой цифрой 0, если секунд меньше 10
		   if(seconds < 10){ 
			   secondsString = "0" + seconds;
		   }else{
			   secondsString = "" + seconds;}
		   
		   finalTimerString = finalTimerString + minutes + ":" + secondsString;
		

		return finalTimerString;
	}
	
	/**
	 * Метод для того, чтобы получить процент проигранного времени.
	 *
	 * */
	public int getProgressPercentage(long currentDuration, long totalDuration){
		Double percentage = (double) 0;
		
		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);
		
		// расчёт процента
		percentage =(((double)currentSeconds)/totalSeconds)*100;
		

		return percentage.intValue();
	}

	/**
	 * Метод для согласования процента проигранного времени с таймером песни
	 * возвращает текущую продолжительность в миллисекундах
	* */
	public int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double)progress) / 100) * totalDuration);

		return currentDuration * 1000;
	}
}
