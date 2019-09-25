# db2doc
idea插件-导出数据库数据字典doc文档
# 将项目配置成idea插件项目的方法：
将idea生成的.iml文件中的<module/>type修改为“PLUGIN_MODULE”，并添加plugin.xml的位置：\<component name="DevKit.ModuleBuildProperties" url="file://$MODULE_DIR$/resources/META-INF/plugin.xml" />
 # 插件缺陷
插件缺陷：由于插件是用freemarker实现生成doc文档的，所以生成的文档比一般的会大很多。导致打开加载很慢，需要另存一下，才能正常使用。