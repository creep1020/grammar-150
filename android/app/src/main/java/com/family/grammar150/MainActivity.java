package com.family.grammar150;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.*;
import org.json.JSONObject;
import java.util.ArrayList;
public class MainActivity extends Activity {
 private SpeechRecognizer recognizer; private String pendingId;
 private WebView web; private TextToSpeech tts; private volatile boolean ready;
 private static final String URL = "https://creep1020.github.io/grammar-150/";
 @Override public void onCreate(Bundle b){super.onCreate(b); web=new WebView(this);setContentView(web);
 WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);
 tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS)ready=tts.setLanguage(Locale.US)>=0;});
 web.addJavascriptInterface(new Bridge(),"IlbbangAndroid");
 web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !r.getUrl().toString().startsWith(URL);}});
 web.setWebChromeClient(new WebChromeClient(){
 @Override public void onPermissionRequest(PermissionRequest request){runOnUiThread(()->{
  android.net.Uri origin=request.getOrigin();
  if(!"https".equals(origin.getScheme())||!"creep1020.github.io".equals(origin.getHost())){request.deny();return;}
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED && java.util.Arrays.asList(request.getResources()).contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE))request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
  else request.deny();
 });}
 });
 web.loadUrl(URL); }
 private void status(String id,String state){web.evaluateJavascript("window.onIlbbangNativeVoiceStatus?.("+JSONObject.quote(id)+","+JSONObject.quote(state)+")",null);}
 private void finishVoice(String id,String transcript,String error){
  if(!id.equals(pendingId))return; pendingId=null;
  if(recognizer!=null){recognizer.destroy();recognizer=null;}
  JSONObject result=new JSONObject();try{result.put("requestId",id);result.put("transcript",transcript);result.put("error",error);}catch(Exception ignored){}
  web.evaluateJavascript("window.onIlbbangNativeVoiceResult?.("+result.toString()+")",null);
 }
 private void beginVoice(String id){
  if(pendingId!=null)return; pendingId=id;status(id,"preparing");
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},101);return;}
  launchVoice(id);
 }
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);if(code==101&&pendingId!=null){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)launchVoice(pendingId);else finishVoice(pendingId,"","마이크 권한이 필요합니다.");}}
 private void launchVoice(final String id){
  if(!SpeechRecognizer.isRecognitionAvailable(this)){finishVoice(id,"","음성인식 서비스가 없습니다.");return;}
  try{recognizer=SpeechRecognizer.createSpeechRecognizer(this);recognizer.setRecognitionListener(new RecognitionListener(){
   public void onReadyForSpeech(Bundle b){if(id.equals(pendingId))status(id,"recording");}
   public void onBeginningOfSpeech(){} public void onRmsChanged(float f){} public void onBufferReceived(byte[] b){}
   public void onEndOfSpeech(){if(id.equals(pendingId))status(id,"recognizing");}
   public void onError(int e){finishVoice(id,"","음성인식 오류: "+e);}
   public void onResults(Bundle b){ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);finishVoice(id,a==null||a.isEmpty()?"":a.get(0),"");}
   public void onPartialResults(Bundle b){} public void onEvent(int e,Bundle b){}
  });Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");recognizer.startListening(i);}catch(Exception e){finishVoice(id,"","음성인식을 시작하지 못했습니다.");}
 }
 public class Bridge {
 @JavascriptInterface public void startRecording(String id,String mode){runOnUiThread(()->beginVoice(id));}
 @JavascriptInterface public void stopRecording(String id){runOnUiThread(()->{if(id.equals(pendingId)&&recognizer!=null){status(id,"recognizing");recognizer.stopListening();}});}
 @JavascriptInterface public void cancelRecording(String id){runOnUiThread(()->{if(id.equals(pendingId)){pendingId=null;if(recognizer!=null){recognizer.cancel();recognizer.destroy();recognizer=null;}}});}
  @JavascriptInterface public boolean canSpeakEnglish(){return ready;} @JavascriptInterface public void speak(String text,float rate){runOnUiThread(()->{if(ready){tts.setSpeechRate(rate);tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"grammar");}});} @JavascriptInterface public void stopSpeaking(){runOnUiThread(()->{if(tts!=null)tts.stop();});} }
 @Override protected void onDestroy(){if(recognizer!=null)recognizer.destroy();if(tts!=null)tts.shutdown();web.destroy();super.onDestroy();}
}
