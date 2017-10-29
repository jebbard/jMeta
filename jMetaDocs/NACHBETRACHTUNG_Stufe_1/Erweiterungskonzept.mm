<map version="0.8.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1300810177796" ID="Freemind_Link_1293513579" MODIFIED="1300811753062" TEXT="Erweiterungskonzept">
<font BOLD="true" NAME="SansSerif" SIZE="16"/>
<node CREATED="1300810199984" ID="_" MODIFIED="1300810205015" POSITION="right" TEXT="Definition &quot;Erweiterung&quot;">
<node CREATED="1300810301218" ID="Freemind_Link_786007246" MODIFIED="1300811765937" TEXT="In sich abgeschlossene Komponente, &#xa;welche die bestehende Funktionalit&#xe4;t &#xa;der Library erweitert"/>
</node>
<node CREATED="1300810205859" ID="Freemind_Link_885174047" MODIFIED="1300810339500" POSITION="left" TEXT="Arten der Erweiterung (technisch)">
<node CREATED="1300810576171" ID="Freemind_Link_1809765707" MODIFIED="1300810798984" TEXT="Dynamische Erweiterung: Setzen neuer, &#xa;eigener Implementierungen f&#xfc;r bestimmte &#xa;Aspekte &#xfc;ber eine Library-Methode">
<node CREATED="1300810800765" ID="Freemind_Link_1867520927" MODIFIED="1300810804281" TEXT="ITransformationHandler"/>
<node CREATED="1300811267937" ID="Freemind_Link_796950842" MODIFIED="1300811280093" TEXT="Konvertierer und Mapper f&#xfc;r Metadatenformate"/>
<node CREATED="1300811310234" ID="Freemind_Link_1365015401" MODIFIED="1300811316140" TEXT="Validierer f&#xfc;r Datenformat"/>
</node>
<node CREATED="1300810999781" ID="Freemind_Link_357774820" MODIFIED="1300811037500" TEXT="Statische Erweiterung: Bereitstellen &#xa;neuer Inhalte als eigenst&#xe4;ndige &#xa;Deployment-Einheiten, die beim Starten &#xa;der Library geladen werden"/>
</node>
<node CREATED="1300810210921" ID="Freemind_Link_332908429" MODIFIED="1300810225890" POSITION="right" TEXT="Potentielle erweiterbare Teile der Library">
<node CREATED="1300812022109" ID="Freemind_Link_1150812238" MODIFIED="1300812051406" TEXT="IDataFormatSpecification/ISpecificationLoader"/>
<node CREATED="1300812057171" ID="Freemind_Link_907323535" MODIFIED="1300812077234" TEXT="IDataBlockFactory/IInternalDatablockFactory"/>
<node CREATED="1300812078125" ID="Freemind_Link_1027859366" MODIFIED="1300812091796" TEXT="IDataBlockReader"/>
<node CREATED="1300812092875" ID="Freemind_Link_1652084732" MODIFIED="1300812096078" TEXT="IDataBlockWriter"/>
<node CREATED="1300812097140" ID="Freemind_Link_1602084394" MODIFIED="1300812245031" TEXT="IField oder FieldConverter"/>
<node CREATED="1300812176250" ID="Freemind_Link_550783686" MODIFIED="1300812180109" TEXT="IMediumFactory"/>
<node CREATED="1300812102578" ID="Freemind_Link_324827855" MODIFIED="1300812129203" TEXT="ITransformationHandler">
<icon BUILTIN="button_ok"/>
<node CREATED="1300812112515" ID="Freemind_Link_1608744884" MODIFIED="1300812117796" TEXT="Erweiterung durch setter"/>
</node>
<node CREATED="1300812150593" ID="Freemind_Link_1444258260" MODIFIED="1300812190390" TEXT="Validierer">
<icon BUILTIN="help"/>
</node>
<node CREATED="1300812156000" ID="Freemind_Link_505851013" MODIFIED="1300812190390" TEXT="Converter/Mapper">
<icon BUILTIN="help"/>
</node>
<node CREATED="1300812166078" ID="Freemind_Link_543654773" MODIFIED="1300812190390" TEXT="Format-Bestandteil">
<icon BUILTIN="help"/>
</node>
</node>
<node CREATED="1300810340312" HGAP="26" ID="Freemind_Link_172303668" MODIFIED="1300812004953" POSITION="left" TEXT="Arten der Erweiterung (inhaltich)" VSHIFT="-37">
<node CREATED="1300811052406" ID="Freemind_Link_1970276115" MODIFIED="1300811060546" TEXT="Container-Datenformat"/>
<node CREATED="1300811061265" ID="Freemind_Link_1435581774" MODIFIED="1300811069218" TEXT="Metadaten-Format"/>
<node CREATED="1300811070250" ID="Freemind_Link_1052772688" MODIFIED="1300811099609" TEXT="Medium"/>
<node CREATED="1300811121171" ID="Freemind_Link_1451875537" MODIFIED="1300811132062" TEXT="Format-Bestandteil (Container, Attribut etc.)"/>
<node CREATED="1300811283796" ID="Freemind_Link_332888257" MODIFIED="1300811292281" TEXT="Konvertierer und Mapper f&#xfc;r Metadatanformat"/>
<node CREATED="1300811304187" ID="Freemind_Link_124536546" MODIFIED="1300811308546" TEXT="Validierer f&#xfc;r Datenformat"/>
</node>
<node CREATED="1300810359093" ID="Freemind_Link_526321685" MODIFIED="1300810379875" POSITION="right" TEXT="Ziele/Anforderungen">
<node CREATED="1300810361593" ID="Freemind_Link_1163043369" MODIFIED="1300810402656" TEXT="Unkompliziertheit - Schreiben von Code ist nur n&#xf6;tig bei sehr speziellen funktionalen Erweiterungen"/>
<node CREATED="1300810465546" ID="Freemind_Link_1429383940" MODIFIED="1300810500312" TEXT="Unterst&#xfc;tzung weiterer Container-Formate"/>
<node CREATED="1300810501015" ID="Freemind_Link_1734374711" MODIFIED="1300810510312" TEXT="Unterst&#xfc;tzung weiterer Metadaten-Formate"/>
<node CREATED="1300810511031" ID="Freemind_Link_378412654" MODIFIED="1300810516937" TEXT="Unterst&#xfc;tzung weiterer Medien"/>
<node CREATED="1300811402921" ID="Freemind_Link_655930840" MODIFIED="1300811443921" TEXT="Flexible Erweiterungsm&#xf6;glichkeiten: &#xa;Sinnvolle Erweiterbarkeit an Stellen, &#xa;an denen der Standardmechanismus &#xa;f&#xfc;r ein Datenformat nicht ausreichend &#xa;sein k&#xf6;nnte"/>
<node CREATED="1300811717031" ID="Freemind_Link_1783002415" MODIFIED="1300811744578" TEXT="Nachvollziehbarkeit: Welche Erweiterungen &#xa;sind vorhanden? Sind Erweiterungen fehlerhaft?"/>
</node>
<node CREATED="1300811171953" ID="Freemind_Link_1630310944" MODIFIED="1300811177046" POSITION="left" TEXT="Voraussetzungen">
<node CREATED="1300811178281" ID="Freemind_Link_1761053597" MODIFIED="1300811243968" TEXT="Dynamisches Laden von Implementierungen, &#xa;die nicht auf dem Klassenpfad liegen">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1300811216750" ID="Freemind_Link_207122179" MODIFIED="1300811247703" TEXT="Definition von jar-File-Beziehungen">
<icon BUILTIN="help"/>
</node>
<node CREATED="1300811325093" ID="Freemind_Link_42912403" MODIFIED="1300811777750" TEXT="Definition von Package- und Projektstrukturen">
<icon BUILTIN="help"/>
</node>
</node>
<node CREATED="1300811472593" ID="Freemind_Link_1932740935" MODIFIED="1300811475109" POSITION="right" TEXT="Architektur">
<node CREATED="1300811475843" ID="Freemind_Link_55822960" MODIFIED="1300811481265" TEXT="Library &quot;Kern&quot;">
<node CREATED="1300811552062" ID="Freemind_Link_130244665" MODIFIED="1300811554203" TEXT="Context"/>
<node CREATED="1300811489843" ID="Freemind_Link_461079685" MODIFIED="1300811506890" TEXT="DataBlocks"/>
<node CREATED="1300811494281" ID="Freemind_Link_919546781" MODIFIED="1300811497390" TEXT="Media"/>
<node CREATED="1300811498125" ID="Freemind_Link_1844554876" MODIFIED="1300811504375" TEXT="DataFormats"/>
<node CREATED="1300811507656" ID="Freemind_Link_1186812531" MODIFIED="1300811510406" TEXT="Metadata"/>
<node CREATED="1300811511093" ID="Freemind_Link_944190653" MODIFIED="1300811514421" TEXT="Validation"/>
<node CREATED="1300811515296" ID="Freemind_Link_1020439395" MODIFIED="1300811538734" TEXT="Conversion"/>
<node CREATED="1300811542062" ID="Freemind_Link_372848548" MODIFIED="1300811549406" TEXT="ExtensionManagement"/>
</node>
<node CREATED="1300811482046" ID="Freemind_Link_1028015963" MODIFIED="1300811484375" TEXT="Erweiterungen">
<node CREATED="1300811834875" ID="Freemind_Link_984024911" MODIFIED="1300811843968" TEXT="Datenformate Standardumfang"/>
<node CREATED="1300811845265" ID="Freemind_Link_1144956619" MODIFIED="1300811852796" TEXT="Medien Standardumfang"/>
</node>
</node>
<node CREATED="1300812370031" ID="Freemind_Link_1732523540" MODIFIED="1300812373484" POSITION="left" TEXT="Projektstruktur">
<node CREATED="1300812375625" ID="Freemind_Link_525521666" MODIFIED="1300812503562" TEXT="Library_Core_Interface: Benutzer-Schnittstelle des Library-Kerns"/>
<node CREATED="1300812454984" ID="Freemind_Link_990796144" MODIFIED="1300812491812" TEXT="Library_Core_Export: Erweiterungs-Schnittstelle des Library-Kerns"/>
<node CREATED="1300812416859" ID="Freemind_Link_1746915326" MODIFIED="1300812427968" TEXT="Library_Core_Impl: Implementierung des Library-Kerns"/>
<node CREATED="1300812539781" ID="Freemind_Link_308368166" MODIFIED="1300812561609" TEXT="Library_Extensions_Standard: Standard-Erweiterungen"/>
<node CREATED="1300812562750" ID="Freemind_Link_1093880259" MODIFIED="1300812575359" TEXT="Library_Extensions_X: Third Party Erweiterungen"/>
</node>
</node>
</map>
