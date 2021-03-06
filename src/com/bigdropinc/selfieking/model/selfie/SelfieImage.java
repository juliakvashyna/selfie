package com.bigdropinc.selfieking.model.selfie;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@DatabaseTable(tableName = "selfie")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfieImage implements Parcelable {

    @DatabaseField(id = true, generatedId = false)
    private int id;
    @DatabaseField
    private String image;
    @DatabaseField
    private String imageSmall;
    @DatabaseField
    private String imageMedium;
    @DatabaseField
    private String token;
    @DatabaseField
    private String date;
    @DatabaseField
    private String description;
    @DatabaseField
    private String location;
    @DatabaseField
    private int like;
    @DatabaseField
    private int comment;
    @DatabaseField
    private boolean liked;
    @DatabaseField
    public int inContest;
    @DatabaseField
    private float rate;
    private Star stars;
    @DatabaseField
    private String userAvatar;
    @DatabaseField
    private int userId;
    @DatabaseField
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public boolean isInContest() {
        return inContest == 1;
    }

    public void setInContest(int inContest) {

        this.inContest = inContest;
    }

    private byte[] bytesImage;

    public SelfieImage() {
    }

    public SelfieImage(int id) {
        super();
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLikes() {
        return like;
    }

    public void setLikes(int likes) {
        this.like = likes;
    }

    public void setComments(int comments) {
        this.comment = comments;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getBytesImage() {
        return bytesImage;
    }

    public void setBytesImage(byte[] bytesImage) {
        this.bytesImage = bytesImage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String url) {
        this.image = url;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    public String getImageMedium() {
        return imageMedium;
    }

    public void setImageMedium(String imageMedium) {
        this.imageMedium = imageMedium;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SelfieImage [id=" + id + ", image=" + image + ", token=" + token + " ]";
    }

    // public void convertBitmapToByte() {
    // ByteArrayOutputStream stream = new ByteArrayOutputStream();
    // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    // bytesImage = stream.toByteArray();
    //
    // }
    //
    // public void convertByteToBitmap() {
    //
    // byte[] decodedByte = Base64.decode(bytesImage, 0);
    // bitmap = BitmapFactory.decodeByteArray(decodedByte, 0,
    // decodedByte.length);
    //
    // }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(token);
        dest.writeString(description);
        dest.writeInt(like);
        dest.writeInt(comment);
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeString(date);
        dest.writeParcelable(stars, 0);
        dest.writeInt(userId);
        dest.writeString(userAvatar);
        // dest.writeByteArray(bytesImage);
    }

    public static final Parcelable.Creator<SelfieImage> CREATOR = new Parcelable.Creator<SelfieImage>() {
        public SelfieImage createFromParcel(Parcel in) {
            return new SelfieImage(in);
        }

        public SelfieImage[] newArray(int size) {
            return new SelfieImage[size];
        }
    };

    private SelfieImage(Parcel in) {
        id = in.readInt();
        image = in.readString();
        token = in.readString();
        description = in.readString();
        like = in.readInt();
        comment = in.readInt();
        liked = in.readByte() != 0;
        date = in.readString();
        stars = in.readParcelable(null);
        userId = in.readInt();
        userAvatar = in.readString();
        // in.readByteArray(bytesImage);
        // convertByteToBitmap();

    }

    public Star getStars() {
        return stars;
    }

    public void setStars(Star stars) {
        this.stars = stars;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int hashCode() {

        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SelfieImage other = (SelfieImage) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
