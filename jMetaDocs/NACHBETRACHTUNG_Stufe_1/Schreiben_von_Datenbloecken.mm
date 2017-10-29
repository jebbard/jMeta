<map version="0.8.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1300627436359" ID="Freemind_Link_1815525828" MODIFIED="1300628775109" TEXT="Schreiben von&#xa;Datenbl&#xf6;cken">
<font BOLD="true" NAME="SansSerif" SIZE="24"/>
<node CREATED="1300627488625" ID="_" MODIFIED="1300628621625" POSITION="right" TEXT="Arten">
<font BOLD="true" NAME="SansSerif" SIZE="16"/>
<node CREATED="1300627521515" ID="Freemind_Link_516712464" MODIFIED="1300627527406" TEXT="Bestehende Datenbl&#xf6;cke &#xe4;ndern"/>
<node CREATED="1300627528031" ID="Freemind_Link_65082627" MODIFIED="1300627536187" TEXT="Bestehende Datenbl&#xf6;cke l&#xf6;schen"/>
<node CREATED="1300627536906" ID="Freemind_Link_324938009" MODIFIED="1300627544000" TEXT="Neue Datenbl&#xf6;cke hinzuf&#xfc;gen"/>
</node>
<node CREATED="1300627494937" ID="Freemind_Link_1240110910" MODIFIED="1300628621625" POSITION="left" TEXT="Kontext">
<font BOLD="true" NAME="SansSerif" SIZE="16"/>
<node CREATED="1300627499187" ID="Freemind_Link_687411325" MODIFIED="1300627503109" TEXT="Caching">
<node CREATED="1300627507031" ID="Freemind_Link_253163301" MODIFIED="1300627515625" TEXT="Relevant f&#xfc;r Caching">
<icon BUILTIN="help"/>
<node CREATED="1300628546312" ID="Freemind_Link_1607623427" MODIFIED="1300628555828" TEXT="Aus aktueller Sicht: Nein"/>
</node>
</node>
<node CREATED="1300627503890" ID="Freemind_Link_958207078" MODIFIED="1300627506218" TEXT="API"/>
</node>
<node CREATED="1300627550234" ID="Freemind_Link_1569092345" MODIFIED="1300628621625" POSITION="left" TEXT="Fehlerbedingungen">
<font BOLD="true" NAME="SansSerif" SIZE="16"/>
<node CREATED="1300627559796" ID="Freemind_Link_1336083631" MODIFIED="1300627571343" TEXT="Schreibversuch auf Read-Only Medium"/>
<node CREATED="1300627572125" ID="Freemind_Link_405376994" MODIFIED="1300628603812" TEXT="Medium wurde in der Zwischenzeit ge&#xe4;ndert&#xa;(z.B. Offset des zu schreibenden &#xa;Blocks hat sich verschoben)"/>
<node CREATED="1300627791828" ID="Freemind_Link_785924842" MODIFIED="1300628581750" TEXT="Zu schreibende Daten entsprechen &#xa;nicht der Datenformat-Spezifikation"/>
<node CREATED="1300627809671" ID="Freemind_Link_161872722" MODIFIED="1300628398015" TEXT="Speicherplatz auf dem Medium ist ersch&#xf6;pft"/>
</node>
<node CREATED="1300627545265" ID="Freemind_Link_1623290220" MODIFIED="1300628621625" POSITION="right" TEXT="Features">
<font BOLD="true" NAME="SansSerif" SIZE="16"/>
<node CREATED="1300627825218" ID="Freemind_Link_309603994" MODIFIED="1300627884937" TEXT="&#xc4;nderungen und Blockhierarchie k&#xf6;nnen&#xa;im Speicher zusammengestellt werden, &#xa;ohne sofortige Persistierung"/>
<node CREATED="1300627886968" ID="Freemind_Link_383159950" MODIFIED="1300627927265" TEXT="&quot;Einf&#xfc;gen&quot; neuer Daten in starre Medien&#xa;(Daten dahinter m&#xfc;ssen neu geschrieben werden)"/>
<node CREATED="1300627929078" ID="Freemind_Link_1851913351" MODIFIED="1300628066906" TEXT="Verwendung von Padding-Bl&#xf6;cken, falls vorhanden"/>
<node CREATED="1300628068046" ID="Freemind_Link_1972238371" MODIFIED="1300628592125" TEXT="B&#xfc;ndelung von mehreren &#xc4;nderungen &#xa;in einer oder wenigen Schreiboperationen"/>
<node CREATED="1300628087343" ID="Freemind_Link_772646963" MODIFIED="1300628098343" TEXT="Komfortable Erzeugung neuer Container und Felder"/>
<node CREATED="1300628099218" ID="Freemind_Link_1085356117" MODIFIED="1300628139203" TEXT="Automatische Einhaltung der Datenformat-Spezifikation:&#xa;Abweichende Daten D&#xdc;RFEN NICHT geschrieben werden">
<node CREATED="1300628160375" ID="Freemind_Link_1135108214" MODIFIED="1300628172421" TEXT="SIZE_OF-Felder automatisch berechnen"/>
<node CREATED="1300628173265" ID="Freemind_Link_1547852662" MODIFIED="1300628182000" TEXT="COUNT_OF-Felder automatisch berechnen"/>
<node CREATED="1300628182968" ID="Freemind_Link_1852436109" MODIFIED="1300628238000" TEXT="TRANSFORMATION_OF: Bei bestimmten Werten&#xa;eines Feldes (z.B. Flags): Automatische Durchf&#xfc;hrung &#xa;von Transformationen vor dem Schreiben"/>
<node CREATED="1300628245609" ID="Freemind_Link_1130308703" MODIFIED="1300628264125" TEXT="CRC_32_OF: Berechnung von CRCs"/>
<node CREATED="1300628295328" ID="Freemind_Link_1410861083" MODIFIED="1300628348125" TEXT="PRESENCE_OF: Entsprechend angezeigter &#xa;Datenblock wird mit Default-Wert hinzugef&#xfc;gt, &#xa;wenn nicht bereits in der Block-Hierarchie vorhanden"/>
<node CREATED="1300628264937" ID="Freemind_Link_285719665" MODIFIED="1300628275171" TEXT="Terminierungs-Bytes werden automatisch eingef&#xfc;gt"/>
</node>
<node CREATED="1300628145500" ID="Freemind_Link_202982581" MODIFIED="1300628149234" TEXT="Setzen von Rohbytes"/>
</node>
<node CREATED="1300628461156" ID="Freemind_Link_1797774222" MODIFIED="1300628621609" POSITION="left" TEXT="Offene Punkte">
<font BOLD="true" NAME="SansSerif" SIZE="16"/>
<node CREATED="1300628470140" ID="Freemind_Link_1316652354" MODIFIED="1300628481984" TEXT="Validierung zu schreibender Daten: Wo und Wann?">
<icon BUILTIN="help"/>
</node>
<node CREATED="1300628718765" ID="Freemind_Link_872025661" MODIFIED="1300628818015" TEXT="Synchronisierung mit dem Medium: &#xa;Wie erkennen, dass sich das Medium bereits ge&#xe4;ndert hat?">
<icon BUILTIN="help"/>
</node>
<node CREATED="1300628762468" ID="Freemind_Link_1688809339" MODIFIED="1300628818015" TEXT="M&#xfc;ssen f&#xfc;r das Schreiben alle Daten eines &#xa;Blockes neu gelesen werden? Was ist dann &#xa;mit sehr gro&#xdf;en Bl&#xf6;cken?">
<icon BUILTIN="help"/>
</node>
<node CREATED="1301506273937" ID="Freemind_Link_1902953965" MODIFIED="1301506292750" TEXT="MediumCache auch beim Schreiben verwenden?">
<icon BUILTIN="help"/>
</node>
<node CREATED="1301506300046" ID="Freemind_Link_1688270381" MODIFIED="1301506325765" TEXT="Wie Schreiben mit dem Iterator-Konzept kombinieren?">
<icon BUILTIN="help"/>
</node>
</node>
</node>
</map>
