package com.parser.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.parser.annotations.MQField;

/**
 * Utility class which parse input MQ telegram to corresponding java object. This class handles
 * primitive , Wrapper , Array ,List and Composite type
 * @author prbehera <br>
 *         $Id: MQUtil.java,v 1.1.2.12.2.5 2015-05-26 07:13:25 prbehera Exp $
 * @version 1.$Revision: 1.1.2.12.2.5 $
 */
public class StringParserUtil {

	/** Starting position of telegram name */
	private static final int TELEGRAM_START_POS=8;

	/** End position of telegram name */
	private static final int TELEGRAM_NAME_LENGTH=8;

	/** Starting position of telegram name */
	private static final int USERID_START_POS=121;

	/** End position of telegram name */
	private static final int USERID_NAME_LENGTH=8;

	/** Total length of header */
	private static final int TOTAL_HEADER_LENGTH=139;

	/** Actual telegram message String */
	private String telegramName=new String();

	/** Actual telegram message String */
	private String userID=new String();

	/** Actual telegram message String */
	private String _message=new String();

	/** Current position of String */
	private Integer _position=new Integer(0);

	/** Date format */
	private DateFormat _dateFormatLarge=new SimpleDateFormat("yyyyMMddHHmmssSSS");

	/** Date format */
	private DateFormat _dateFormatSmall=new SimpleDateFormat("yyyyMMdd");

	/**
	 * Map which contains primitive type as key and respective wrapper class as its value
	 */
	public final static Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP=new HashMap<Class<?>, Class<?>>();
	static {
		PRIMITIVE_WRAPPER_MAP.put(boolean.class,Boolean.class);
		PRIMITIVE_WRAPPER_MAP.put(byte.class,Byte.class);
		PRIMITIVE_WRAPPER_MAP.put(short.class,Short.class);
		PRIMITIVE_WRAPPER_MAP.put(char.class,Character.class);
		PRIMITIVE_WRAPPER_MAP.put(int.class,Integer.class);
		PRIMITIVE_WRAPPER_MAP.put(long.class,Long.class);
		PRIMITIVE_WRAPPER_MAP.put(float.class,Float.class);
		PRIMITIVE_WRAPPER_MAP.put(double.class,Double.class);
	}

	/**
	 * Creates a new {@link StringParserUtil}.
	 */
	public StringParserUtil() {
		super();
	}

	/**
	 * Constructor : sets telegram name and actual message . Creates a new {@link StringParserUtil}.
	 * @param message the message received by the telegram
	 */
	public StringParserUtil(String message) {
		this(message,true);
	}

	/**
	 * Constructor : sets telegram name and actual message . Creates a new {@link StringParserUtil}.
	 * @param message the message received by the telegram
	 * @param isWithHeader Given input string is with header or without header
	 */
	public StringParserUtil(String message,boolean isWithHeader) {
		super();
		if (isWithHeader) {
			setTelegramName(message.substring(TELEGRAM_START_POS,TELEGRAM_START_POS+TELEGRAM_NAME_LENGTH));
			_message=message.substring(TOTAL_HEADER_LENGTH,message.length());
			userID=message.substring(USERID_START_POS,USERID_START_POS+USERID_NAME_LENGTH).trim();
		} else {
			_message=message;
		}
	}

	/**
	 * Takes class instance and returns its object with populated vales inside it.
	 * @param inputClass the class instance of expected object
	 * @return populated object
	 * @throws ParserException
	 */
	@SuppressWarnings("rawtypes")
	public Object getReferenceObject(Class inputClass) throws ParserException {

		fillCharacters(inputClass);
		Object object=populateObject(inputClass);

		_message="";
		return object;
	}

	/**
	 * Recursive method used to populate set all the field of the input class
	 * @param inputClass
	 * @return populated object
	 * @throws ParserException
	 */
	@SuppressWarnings("rawtypes")
	private Object populateObject(Class inputClass) throws ParserException {

		Field[] fieldArray=inputClass.getDeclaredFields();
		// Sort all the field of the class
		sortFieldsAsDeclared(fieldArray);

		Object classObject=null;
		Field field=null;
		Class field_type;
		try {
			classObject=inputClass.newInstance();

			for (int pos=0;pos<fieldArray.length;pos++) {
				field=fieldArray[pos];

				if (field.getAnnotation(MQField.class)!=null) {
					field_type=field.getType();
					field.setAccessible(true);

					// Process array type field
					if (field_type.isArray()) {
						processArrayTypeField(classObject,field,fieldArray,pos);
					}
					// Process List type field
					else if (List.class.isAssignableFrom(field_type)) {
						processListTypeField(classObject,field,fieldArray,pos);
					} else {
						setFieldValue(field,classObject);
					}
				}
			}
		} catch (InstantiationException e) {
			throw new ParserException(field.getName(),e);
		} catch (IllegalAccessException e) {
			throw new ParserException(field.getName(),e);
		}
		return classObject;

	}

	/**
	 * processes array type field and populates values inside it. It handles primitive type ,
	 * composite type array
	 * @param classObject
	 * @param field
	 * @param fields
	 * @param pos
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ParserException
	 */
	@SuppressWarnings("rawtypes")
	private void processArrayTypeField(Object classObject,Field field,Field[] fields,int pos) throws IllegalArgumentException,IllegalAccessException,ParserException {

		int numberOfArrayElements=getArraySize(fields,pos);
		Class arrayType=field.getType().getComponentType();

		// If the array type is primitive type then converts it to its wrapper class
		if (arrayType.isPrimitive()) {
			arrayType=PRIMITIVE_WRAPPER_MAP.get(arrayType);
		}

		Object[] object=(Object[])Array.newInstance(arrayType,numberOfArrayElements);

		for (int j=0;j<numberOfArrayElements;j++) {
			object[j]=getCollectionObject(field,field.getType());
		}

		if (arrayType.isPrimitive()) {
			convertAndSetPrimitiveArray(classObject,field,object,arrayType);
		} else {

			field.set(classObject,object);
		}

	}

	/**
	 * Processes {@link List} type field and populates values inside it.
	 * @param classObject
	 * @param field
	 * @param fields
	 * @param pos
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void processListTypeField(Object classObject,Field field,Field[] fields,int pos) throws ParserException,IllegalArgumentException,IllegalAccessException,InstantiationException {

		int numOfNonEmptyElements=getArraySize(fields,pos);
		Class listType=field.getType();
		List list=null;
		Class fieldArgClass=getTypeOfList(field.getGenericType());

		if (List.class.equals(listType)) {
			list=new ArrayList();
		} else {
			list=(List)listType.newInstance();
		}
		for (int j=0;j<numOfNonEmptyElements;j++) {
			list.add(fieldArgClass.cast(getCollectionObject(field,field.getType())));
		}
		field.set(classObject,list);

		// skip next empty collection
		MQField mqField=field.getAnnotation(MQField.class);
		if (mqField.totalCollectionLength()!=0) {
			int listTypeLength=0;
			if (mqField.isComposite()) {
				listTypeLength=getTotalLengthOfClass(fieldArgClass.getDeclaredFields());
			} else {
				listTypeLength=mqField.length();
			}
			_position+=mqField.totalCollectionLength()-(listTypeLength*numOfNonEmptyElements);
		}

	}

	/**
	 * Returns type of List assignable form
	 * @param genericFieldTyp
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Class getTypeOfList(Type genericFieldTyp) {

		Class fieldArgClass=null;

		if (genericFieldTyp instanceof ParameterizedType) {

			ParameterizedType aType=(ParameterizedType)genericFieldTyp;
			Type[] fieldArgTypes=aType.getActualTypeArguments();

			for (Type fieldArgType : fieldArgTypes) {
				fieldArgClass=(Class)fieldArgType;
			}
		}
		return fieldArgClass;
	}

	/**
	 * Returns size of array by taking position of array {@link Field} as input
	 * @param fields
	 * @param pos
	 * @return
	 */
	private int getArraySize(Field[] fields,int pos) {
		int digit_lenght_of_array=fields[pos-1].getAnnotation(MQField.class).length();
		return Integer.parseInt(_message.substring(_position-digit_lenght_of_array,_position).trim());
	}

	/**
	 * Returns object to be set inside array
	 * @param field
	 * @param fieldType
	 * @return
	 * @throws ParserException
	 */
	@SuppressWarnings("rawtypes")
	private Object getCollectionObject(Field field,Class fieldType) throws ParserException {

		MQField mqField=field.getAnnotation(MQField.class);
		int length=mqField.length();
		boolean isComposite=mqField.isComposite();

		if (isComposite&&List.class.isAssignableFrom(fieldType)) {
			return populateObject(getTypeOfList(field.getGenericType()));

		} else if (isComposite&&fieldType.isArray()) {
			return populateObject(fieldType.getComponentType());

		} else {
			Object object=getObjectAccordingToType(fieldType.getComponentType(),length);
			_position+=length;
			return object;
		}
	}

	/**
	 * Sets the value of the field
	 * @param field
	 * @param class_object
	 * @param field_type
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ParserException
	 */
	private void setFieldValue(Field field,Object class_object) throws ParserException {

		MQField mq_field=field.getAnnotation(MQField.class);
		int length=mq_field.length();
		boolean is_composite=mq_field.isComposite();
		try {
			if (is_composite) {
				Object comp_object=populateObject(field.getType());

				field.set(class_object,comp_object);

			} else {
				field.set(class_object,getObjectAccordingToType(field.getType(),length));
				_position+=length;
			}
		} catch (IllegalArgumentException e) {
			throw new ParserException("setFieldValue-"+field.getName(),e);
		} catch (IllegalAccessException e) {
			throw new ParserException("setFieldValue-"+field.getName(),e);
		}

	}

	/**
	 * Cast objects to its respective type
	 * @param fieldType
	 * @param length
	 * @return object of type {@link Object}
	 * @throws ParserException
	 */
	@SuppressWarnings("rawtypes")
	private Object getObjectAccordingToType(Class fieldType,int length) throws ParserException {
		
		String str=_message.substring(_position,_position+length);
		
		if (int.class.equals(fieldType)||Integer.class.equals(fieldType)) {
			return Integer.parseInt(str);
		} else if (String.class.equals(fieldType)) {
			return str.trim();
		} else if (boolean.class.equals(fieldType)||Boolean.class.equals(fieldType)) {
			return BooleanUtils.toBoolean(str);
		} else if (char.class.equals(fieldType)||Character.class.equals(fieldType)) {
			return str.charAt(0);
		} else if (long.class.equals(fieldType)||Long.class.equals(fieldType)) {
			return Long.parseLong(str);
		} else if (double.class.equals(fieldType)||Double.class.equals(fieldType)) {
			return Double.parseDouble(str);
		} else if (float.class.equals(fieldType)||Float.class.equals(fieldType)) {
			return Float.parseFloat(str);
		} else if (BigDecimal.class.equals(fieldType)) {
			return new BigDecimal(str);
		} else if (Date.class.equals(fieldType)) {
			return getDateObject(str);
		} else {
			return str;
		}
	}

	/**
	 * This method converts wrapper array to primitive array and set that primitive array to the
	 * @param classObject
	 * @param field
	 * @param object
	 * @param arrayType
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	private void convertAndSetPrimitiveArray(Object classObject,Field field,Object object,Class arrayType) throws IllegalArgumentException,IllegalAccessException {

		if (int.class.equals(arrayType)) {
			field.set(classObject,ArrayUtils.toPrimitive((Integer[])object));
		} else if (char.class.equals(arrayType)) {
			field.set(classObject,ArrayUtils.toPrimitive((Character[])object));
		} else if (boolean.class.equals(arrayType)) {
			field.set(classObject,ArrayUtils.toPrimitive((Boolean[])object));
		} else if (long.class.equals(arrayType)) {
			field.set(classObject,ArrayUtils.toPrimitive((Long[])object));
		} else if (float.class.equals(arrayType)) {
			field.set(classObject,ArrayUtils.toPrimitive((Float[])object));
		}

	}

	/**
	 * Converts String to date object
	 * @param str date in string format
	 * @return
	 * @throws ParserException
	 */
	private Date getDateObject(String str) throws ParserException {
		Date date=new Date();
		try {
			if (str.length()==8) {
				date=_dateFormatSmall.parse(str);
			} else if (str.length()==16) {
				date=_dateFormatLarge.parse(str+"0");
			} else {
				throw new ParserException("Not a valid date format '"+str+"'");
			}
		} catch (ParseException e) {
			throw new ParserException(e.getMessage());
		}
		return date;
	}

	/**
	 * Sorts array of fields as defined in the class
	 * @param fields Array of {@link Field}
	 */
	private void sortFieldsAsDeclared(Field[] fields) {

		Arrays.sort(fields,new Comparator<Field>() {

			public int compare(Field field1, Field field2) {
				MQField order1=field1.getAnnotation(MQField.class);
				MQField order2=field2.getAnnotation(MQField.class);
				// nulls last
				if (order1!=null&&order2!=null) {
					return order1.position()-order2.position();
				} else if (order1!=null&&order2==null) {
					return -1;
				} else if (order1==null&&order2!=null) {
					return 1;
				}
				return field1.getName().compareTo(field2.getName());
			}

		});
	}

	/**
	 * Fills the empty space if the length of the class is more than length of the telegtam string
	 * @param inputClass
	 */
	@SuppressWarnings("rawtypes")
	private void fillCharacters(Class inputClass) {

		Field[] fields=inputClass.getDeclaredFields();
		sortFieldsAsDeclared(fields);

		int classTotalLength=getTotalLengthOfClass(fields);
		if (_message.length()<=classTotalLength) {
			_message=String.format("%1$-"+classTotalLength+"s",_message);
		} else {
			_message=_message.substring(0,classTotalLength);
		}
	}

	/**
	 * Returns total length of the class
	 * @param fields
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private int getTotalLengthOfClass(Field[] fields) {

		int classTotalLength=0;

		for (int pos=0;pos<fields.length;pos++) {
			Field field=fields[pos];
			
			if (field.getAnnotation(MQField.class)!=null) {

				MQField mqField=field.getAnnotation(MQField.class);
				int fieldLength=mqField.length();
				int collectionLength=0;

				// if the field is an array of composite type
				if (field.getType().isArray()&&mqField.isComposite()) {

					if (mqField.totalCollectionLength()==0) {
						collectionLength=getTotalLengthOfClass(field.getType().getComponentType().getDeclaredFields());
						classTotalLength+=getLengthOfArrayField(fields,classTotalLength,collectionLength,pos);
					} else {
						classTotalLength+=mqField.totalCollectionLength();
					}
					// if the field is list of composite type
				} else if (List.class.isAssignableFrom(field.getType())&&mqField.isComposite()) {

					if (mqField.totalCollectionLength()==0) {
						Class list_type=getTypeOfList(field.getGenericType());
						collectionLength=getTotalLengthOfClass(list_type.getDeclaredFields());
						classTotalLength+=getLengthOfArrayField(fields,classTotalLength,collectionLength,pos);
					} else {
						classTotalLength+=mqField.totalCollectionLength();
					}
				}

				// If the filed is of composite type
				else if (mqField.isComposite()) {
					classTotalLength+=getTotalLengthOfClass(field.getType().getDeclaredFields());
				}

				// if the field is an array or list type
				else if (field.getType().isArray()||Collection.class.isAssignableFrom(field.getType())) {
					if (mqField.totalCollectionLength()==0) {
					classTotalLength+=getLengthOfArrayField(fields,classTotalLength,fieldLength,pos);
					}else{
						classTotalLength+=mqField.totalCollectionLength();
					}
				} else {
					classTotalLength+=fieldLength;
				}
			}
		}
		return classTotalLength;
	}

	/**
	 * Returns the length of the array field
	 * @param fields
	 * @param classTotalLength
	 * @param fieldLength
	 * @param pos
	 * @return
	 */
	private int getLengthOfArrayField(Field[] fields,int classTotalLength,int fieldLength,int pos) {

		int digitLenghtOfArray=fields[pos-1].getAnnotation(MQField.class).length();
		int arraySize=Integer.parseInt(_message.substring(classTotalLength-digitLenghtOfArray,classTotalLength).trim());

		return fieldLength*arraySize;
	}

	/**
	 * Returns the name of telegram
	 * @return telegramName
	 */
	public String getTelegramName() {
		return telegramName;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Sets the name of telegram
	 * @param telegramName
	 */
	private void setTelegramName(String telegramName) {
		this.telegramName=telegramName;
	}

	public String convertObjectToString(Object object) throws ParserException {

		String sb;
		try {
			sb=getStringFromObject(object.getClass(),object);

		} catch (IllegalArgumentException e) {
			throw new ParserException(e);
		} catch (IllegalAccessException e) {
			throw new ParserException(e);
		} catch (InstantiationException e) {
			throw new ParserException(e);

		}

		return sb;
	}

	/**
	 * This utility method converts java object to fixed length string. This is suppotred for all
	 * boolean , Date, List. Array is not supported yet
	 * @param className
	 * @param object
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	private String getStringFromObject(Class className,Object object) throws IllegalArgumentException,IllegalAccessException,InstantiationException {

		StringBuilder sb=new StringBuilder();
		Field[] fields=className.getDeclaredFields();
		sortFieldsAsDeclared(fields);

		for (Field field : fields) {
			MQField mqField=field.getAnnotation(MQField.class);

			if (mqField==null) {
				continue;
			}

			Object fieldObject=null;
			field.setAccessible(true);

			if (object==null) {
				if (int.class.equals(field.getType())||Integer.class.equals(field.getType()))
					fieldObject=0;
				else
					fieldObject=className.newInstance();
			} else {
				fieldObject=field.get(object);
			}

			if (List.class.isAssignableFrom(field.getType())) {

				List list=(List)fieldObject;
				Class listType=getTypeOfList(field.getGenericType());
				sb.append(String.format("%0"+3+"d",list.size()));

				if (mqField.isComposite()) {
					for (Object obj : list) {
						sb.append(getStringFromObject(listType,obj));
					}
				} else {
					for (Object obj : list) {
						appendToString(sb,field,obj);
					}
				}
				// fills empty spaces if totalCollectionLength is specified
				fillEmptyList(listType,field,mqField,list.size(),sb);

			} else if (mqField.isComposite()) {
				sb.append(getStringFromObject(field.getType(),fieldObject));
			} else {
				appendToString(sb,field,fieldObject);
			}
		}
		return sb.toString();
	}

	/**
	 * Fills empty spaces if totalCollectionLength is specified
	 * @param listType
	 * @param field
	 * @param mqField
	 * @param listSize
	 * @param sb
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	private void fillEmptyList(Class listType,Field field,MQField mqField,int listSize,StringBuilder sb) throws IllegalArgumentException,IllegalAccessException,InstantiationException {
		Object emptyObj=listType.newInstance();
		int totalLength=mqField.totalCollectionLength();

		// if total length is not specified do nothing
		if (totalLength==0) {
			return;
		}

		boolean isComposite=mqField.isComposite();
		int emptyListSize=0;

		if (isComposite) {
			emptyListSize=(totalLength/getTotalLengthOfClass(listType.getDeclaredFields()))-listSize;
			for (int i=0;i<emptyListSize;i++) {
				sb.append(getStringFromObject(listType,emptyObj));
			}
		} else {
			emptyListSize=(totalLength/mqField.length())-listSize;
			for (int i=0;i<emptyListSize;i++) {
				appendToString(sb,field,emptyObj);
			}
		}
	}

	/**
	 * @param sb
	 * @param filed
	 * @param object
	 */
	private void appendToString(StringBuilder sb,Field filed,Object object) {
		if (object==null||object=="") {
			sb.append(String.format("%1$"+filed.getAnnotation(MQField.class).length()+"s",""));
		} else if (int.class.equals(filed.getType())||Integer.class.equals(filed.getType())) {
			sb.append(String.format("%0"+filed.getAnnotation(MQField.class).length()+"d",object));
		} else if (Date.class.equals(filed.getType())) {
			//TODO add date format
			sb.append("");
		} else if (boolean.class.equals(filed.getType())||Boolean.class.equals(filed.getType())) {
			sb.append(((Boolean)object).toString());
		} else {
			sb.append(String.format("%1$"+filed.getAnnotation(MQField.class).length()+"s",object));
		}
	}
}
