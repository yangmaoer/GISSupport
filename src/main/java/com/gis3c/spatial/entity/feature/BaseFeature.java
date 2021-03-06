package com.gis3c.spatial.entity.feature;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author hukekuan
 * @Description 具有空间字段的基类
 * @date 2017-07-10 下午3:43
 */
public class BaseFeature implements IFeature {
    private Geometry geometry;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * 获取所有字段信息
     */
    @Override
    public List<Field> AllFieldes(){
        List<Field> fieldList = new ArrayList<>();

        Class superClass = BaseFeature.class;
        Class extendClass = this.getClass();

        while(!superClass.equals(extendClass) && superClass.isAssignableFrom(extendClass)){
            fieldList.addAll(Arrays.asList(extendClass.getDeclaredFields()));
            extendClass = extendClass.getSuperclass();
        }
        fieldList.addAll(Arrays.asList(superClass.getDeclaredFields()));

        return fieldList;
    }


    /**
     * @author hukekuan
     * @Description 根据类信息生成对应的SimpleFeatureType对象
     * @return SimpleFeatureType
     * @throws NullPointerException
     * @date 2017-07-10 下午6:39
     */
    public SimpleFeatureType createFeatureType() throws NullPointerException{
        SimpleFeatureTypeBuilder build = new SimpleFeatureTypeBuilder();
        SimpleFeatureType featureType=null;
        if(!geometryChecked()){
            throw new NullPointerException("空间字段为空");
        }
        //CoordinateReferenceSystem crs = this.geometry..GetCRS();
        CoordinateReferenceSystem crs = null;
        build.setCRS(crs !=null? crs:DefaultGeographicCRS.WGS84);
        build.setName(this.getClass().getSimpleName());
        List<Field> fields = this.AllFieldes();
        fields.forEach(field -> {
            build.add(field.getName(),field.getType());
        });
        featureType = build.buildFeatureType();

        return featureType;
    }

    /**
     * @author hukekuan
     * @Description 生成SimpleFeature对象
     * @param featureType,转换类对应的featureType
     * @param featureIndex,feature序列号
     * @return 对象转SimpleFeature
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @date 2017-07-10 下午7:10
     */
    public SimpleFeature javaBean2SimpleFeature(SimpleFeatureType featureType, String featureIndex)
            throws IllegalArgumentException, IllegalAccessException {
        SimpleFeature simpleFeature = null;
        if(featureType == null){
            throw new IllegalArgumentException("featureType参数为空");
        }
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        List<Field> fields = this.AllFieldes();
        List<Object> objList = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            objList.add(field.get(this));
        }
        simpleFeature = featureBuilder.buildFeature(featureIndex, objList.toArray());
        return simpleFeature;
    }




    /***************************************************************************************************/

    /**
     * @author hukekuan
     * @Description 判断对象是否存在空间字段
     * @return true，存在空间字段；false，不存在
     * @date 2017-07-10 下午4:42
     */
    private boolean geometryChecked(){
        boolean isExist = false;
        if(this.geometry != null){
            isExist = true;
        }

        return isExist;
    }
}
