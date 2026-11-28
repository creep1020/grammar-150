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
 private TextToSpeech bodyTts; private boolean bodyReady; private WebView web; private TextToSpeech tts; private volatile boolean ready;
 private static final String URL = "https://creep1020.github.io/grammar-150/";
 @Override public void onCreate(Bundle b){super.onCreate(b); web=new WebView(this);setContentView(web);
 WebSettings s=web.getSettings();s.setCacheMode(WebSettings.LOAD_DEFAULT);s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);
 tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS)ready=tts.setLanguage(Locale.US)>=0;});
 bodyTts=new TextToSpeech(this,status->{bodyReady=status==TextToSpeech.SUCCESS; if(bodyReady)bodyTts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener(){
 public void onStart(String id){} public void onDone(String id){bodyEvent(id,true);} public void onError(String id){bodyEvent(id,false);}
 });});
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
 web.loadUrl(URL + "?appVersion=101"); }
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
 private String backupPayload;
 private void backupStatus(String message){web.evaluateJavascript("window.grammarBackupStatus?.("+JSONObject.quote(message)+")",null);}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=201&&request!=202)return;String payload=backupPayload;backupPayload=null;if(result!=RESULT_OK||data==null||data.getData()==null){backupStatus("파일 선택을 취소했어요.");return;}try{if(request==201){try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData(),"wt")){if(out==null||payload==null)throw new java.io.IOException();out.write(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));}backupStatus("백업 파일 저장 완료");}else{try(java.io.InputStream in=getContentResolver().openInputStream(data.getData());java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){if(in==null)throw new java.io.IOException();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>2000000)throw new java.io.IOException("파일 크기 초과");out.write(buf,0,n);}web.evaluateJavascript("window.grammarImportBackup?.("+JSONObject.quote(out.toString("UTF-8"))+")",null);}}}catch(Exception e){backupStatus("파일 처리 실패: "+e.getMessage());}}
 public class Bridge {
 @JavascriptInterface public void saveBackup(String name,String text){runOnUiThread(()->{try{backupPayload=text;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,201);}catch(Exception e){backupPayload=null;backupStatus("저장 화면을 열지 못했어요.");}});}
 @JavascriptInterface public void openBackup(){runOnUiThread(()->{try{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,202);}catch(Exception e){backupStatus("파일 선택 화면을 열지 못했어요.");}});}

 @JavascriptInterface public void startRecording(String id,String mode){runOnUiThread(()->beginVoice(id));}
 @JavascriptInterface public void stopRecording(String id){runOnUiThread(()->{if(id.equals(pendingId)&&recognizer!=null){status(id,"recognizing");recognizer.stopListening();}});}
 @JavascriptInterface public void cancelRecording(String id){runOnUiThread(()->{if(id.equals(pendingId)){pendingId=null;if(recognizer!=null){recognizer.cancel();recognizer.destroy();recognizer=null;}}});}
 @JavascriptInterface public void speakBody(String text,String lang,String id){runOnUiThread(()->{if(!bodyReady){bodyEvent(id,false);return;} if(bodyTts.setLanguage(Locale.forLanguageTag(lang))<0){bodyEvent(id,false);return;} bodyTts.setSpeechRate(1f); if(bodyTts.speak(text,TextToSpeech.QUEUE_FLUSH,null,id)==TextToSpeech.ERROR)bodyEvent(id,false);});}
 @JavascriptInterface public void stopBody(){runOnUiThread(()->{if(bodyTts!=null)bodyTts.stop();});}
  @JavascriptInterface public boolean canSpeakEnglish(){return ready;} @JavascriptInterface public void speak(String text,float rate){runOnUiThread(()->{if(ready){tts.setSpeechRate(rate);tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"grammar");}});} @JavascriptInterface public void stopSpeaking(){runOnUiThread(()->{if(tts!=null)tts.stop();});} }
 private void bodyEvent(String id,boolean ok){runOnUiThread(()->web.evaluateJavascript("window.grammarBodyEvent&&window.grammarBodyEvent("+JSONObject.quote(id)+","+ok+")",null));}
 @Override protected void onDestroy(){if(recognizer!=null)recognizer.destroy();if(tts!=null)tts.shutdown();if(bodyTts!=null)bodyTts.shutdown();web.destroy();super.onDestroy();}
}
