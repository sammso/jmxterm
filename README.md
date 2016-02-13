Read more in http://wiki.cyclopsgroup.org/jmxterm
===================================================

Liferay specific:

new command

Following to display Liferay caches that CacheMissPercentage is more than 25%

Example
```
$>jvms
22230    (m) - org.apache.catalina.startup.Bootstrap start
52085    ( ) - jmxterm-1.0.0-SNAPSHOT-uber.jar
$>open 22230
#Connection to 22230 is opened
$>liferaycaches
CacheType                   Bean                                                                                                                                ObjectCount    CacheHits  CacheMisses  CacheMissPercentage
liferay-multi-vm-clustered  com.liferay.portal.kernel.dao.orm.EntityCache.com.liferay.portal.model.impl.LayoutSetPrototypeImpl                                            0            0            2                  100
liferay-multi-vm-clustered  com.liferay.portal.kernel.dao.orm.EntityCache.com.liferay.portal.model.impl.PortletImpl                                                       0           99           99                   50
liferay-multi-vm-clustered  com.liferay.portal.kernel.dao.orm.EntityCache.com.liferay.portal.model.impl.ServiceComponentImpl                                              0            0            1                  100
liferay-multi-vm-clustered  com.liferay.portal.kernel.dao.orm.FinderCache.com.liferay.portal.model.impl.BackgroundTaskImpl.List2                                          0            0            1                  100
```

To compile:
-----------
Test are still brokes, so it isi necessary to without tests


```
mvn clean install -Dmaven.test.skip=true
```