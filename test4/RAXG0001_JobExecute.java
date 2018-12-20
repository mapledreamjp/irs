/**
 *  システム名称      ASS新事務センター対応
 *  クラス名          RAXG0001_JobExecute
 *  クラス名称        共通Job起動クラス
 *  作成日            2011/12/6
 *  作成者            VIXUS H.Kano
 * @author  $Author: s.murayama $
 * @version $Revision: 1.2 $ <br>
 *          $Date: 2018/08/08 01:53:37 $
 */
package dis.RAXG.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dis.RAXG.common.RAXG0001_Constants;
import tec.lx50.bse.common.etc.Log;
//import tec.lx50.bse.jcl.api.Operator;
//import tec.lx50.bse.jcl.api.Factory;
import tec.lx50.bse.common.sys.BseVstore;
import tec.lx50.bse.common.sys.Resource;

/**
 * 共通Job起動のクラス
 *
 */

public class RAXG0001_JobExecute implements RAXG0001_Constants{
	/**
	* job_idおよび引数を取得してバッチ処理を起動します。
	*
	* @param  job_id
	*             起動ジョブＩＤ
	* @param  jobs
	*             受け渡す引数(ジョブＩＤ以外)
	* @throws Exception
	*/
	public void funcExcute(String job_id, String[] jobs) throws Exception{

		//JOB起動の為のメソッドです。以下にJOB起動の内容を記述してください。
		//エラーはthrowsしてくれれば戻り値booleanをとらなくても大丈夫と思われるので戻り値voidにしました。

		/**
		// コマンド生成
		StringBuffer job = new StringBuffer();

		job.append(ADD_JOB);
		job.append(job_id);
		job.append(TIME_JOB);
		for (int i = 0; i < jobs.length; i++) {
			job.append(jobs[i]);
			if ((i + 1) < jobs.length) {
				job.append(DIVIDE);
			}
		}
		**/

		/*
		 *論理店
		 */
		String STORECD = "%VSTORECD%";
	    /** プロパティファイル取得向け **/
		String RESOURCE = "resource";

		/** プロパティファイル取得向け	 **/
		String APPLICATION = "application_raxg";

		String JP1_NETROOT = "root_jp1";
		String JP1_SHLPGM = "shl_jp1";
		String JP1_SHLDIR = "shl_dir";
		//int ret         	= 9;
		String strRet="";

		try{

			String Cmnd			= "";
			String strJp1 		= Resource.getString(RESOURCE, APPLICATION, JP1_NETROOT);	//JP1登録ジョブネットルート(ASS_MA等)
			String strBat 		= Resource.getString(RESOURCE, APPLICATION, JP1_SHLPGM);	//JP1登録ジョブ起動シェル
			String strDir 		= Resource.getString(RESOURCE, APPLICATION, JP1_SHLDIR);	//シェルディレクトリ
			String vStorecd 	= BseVstore.getInstance().getVstorecd();
			String strJp1Param	= "";

			if (vStorecd == null) {
				vStorecd = "000002"; // テスト用（ローカル）
			}

			strDir				= strDir.replaceAll(STORECD, vStorecd);
			Cmnd  				= strDir+strBat;

			Log.debug("RAXG0001_JobExecute","【ID_xx】JP1実行プログラム："+strBat);
			Log.debug("RAXG0001_JobExecute","【ID_xx】JP1実行ディレクトリ："+strDir);
			Log.debug("RAXG0001_JobExecute","【ID_xx】論理店舗："+vStorecd);
			Log.debug("RAXG0001_JobExecute","【ID_xx】実行：" + Cmnd);

			List<String> command=new ArrayList<>();;

			command.add(Cmnd); //0. 実行シェル
			command.add(strJp1); //1. 引数にJP1ジョブルート（＝ＪＰ１登録ジョブルート）※ASS_MA等
			command.add(job_id); //2. 引数にジョブＩＤ（＝ＪＰ１登録ジョブネット）
			command.add(vStorecd); //3. 引数に論理店（＝ＪＰ１実行登録ユーザ）※シェル側で tec-xxxxxxxに置き換えsuし実行

			//JP1ｼﾞｮﾌﾞ実行時の引継ぎ引数を生成
			//JP1中のジョブネットの引継引数は AJS2xxxx:値　の形式でJP1のジョブ実行登録（ajsentry）コマンドで
			//指定する。
			for (int iloop = 0; iloop < jobs.length; iloop++) {
				//strJp1Param = strJp1Param + "-c 'AJS2PARA" + Integer.toString(iloop+1) + ":" + jobs[iloop] + "' ";
				command.add(jobs[iloop]); //4以降. JP1定義済みジョブへの引き継ぎパラメータ
			}
			//command.add(strJp1Param); //4. JP1定義済みジョブへの引き継ぎパラメータ


			// Runtimeを使用する場合
			//Runtime run = Runtime.getRuntime();
			//Process pro = run.exec(Cmnd);
			//終了待ち
		    //ret = pro.waitFor();
		    //pro.destroy();  // 子プロセスを明示的に終了させ、資源を回収できるようにする

			//ProcessBuilder b = new ProcessBuilder(Cmnd,jobs.toString());
			ProcessBuilder b = new ProcessBuilder(command);
			b.redirectErrorStream(true);
			Process p = b.start();

			//InputStreamのスレッド開始
			RAXG0001_InputStreamThread it = new RAXG0001_InputStreamThread(p.getInputStream());
			RAXG0001_InputStreamThread et = new RAXG0001_InputStreamThread(p.getErrorStream());
			it.start();
			et.start();

			//プロセスの終了待ち
			p.waitFor();

			//InputStreamのスレッド終了待ち
			it.join();
			et.join();

			System.out.println("戻り値：" + p.exitValue());

			//標準出力の内容を出力
			for (String s : it.getStringList()) {
				System.out.println(s);
			}
			//標準エラーの内容を出力
			for (String s : et.getStringList()) {
				System.err.println(s);
			}

			/***
			BufferedReader reader = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
			StringBuffer strbuf = new StringBuffer();
			String line = null;
		    //標準エラー出力が標準出力にマージして出力されるので、標準出力だけ読み出せばいい
			while ((line = reader.readLine()) != null) {
				strbuf.append(line + "\n");
			}
			strRet=strbuf.toString();
			**/

			Log.debug("RAXG0001_JobExecute","【ID_xx】終了");
			//結果表示
			Log.debug("RAXG0001_JobExecute","【ID_xx】結果です："+strRet );

		/**
		}catch(InterruptedException e){
			System.err.println("InterruptedException on exec() method");
			e.printStackTrace();
			Log.debug("RAXG0001_JobExecute","【ID_xx】InterruptedException："  +ret + "  "+e);
		**/
		}catch(IOException e){
			System.err.println("IOException on exec() method");
			e.printStackTrace();
			Log.debug("RAXG0001_JobExecute","【ID_xx】IOException：["  +strRet + "]["  +e + "]");
			throw e;
		}catch(Exception e){
			System.err.println("Exception on exec() method");
			e.printStackTrace();
			Log.debug("RAXG0001_JobExecute","【ID_xx】Exception：["  +strRet + "]["  +e + "]");
			throw e;
		}finally {

		}

	}
}
