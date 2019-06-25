package orangeschool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.security.MessageDigest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import orangeschool.WebUtil;
import orangeschool.form.ImageUploadForm;
import orangeschool.form.TextContentForm;
import orangeschool.form.TopicForm;
import orangeschool.form.CategoryForm;
import orangeschool.model.Admin;
import orangeschool.model.Category;
import orangeschool.model.ImageContent;
import orangeschool.model.SoundContent;
import orangeschool.model.Topic;
import orangeschool.repository.ImageContentRepository;
import orangeschool.service.AdminService;
import orangeschool.service.ImageContentService;
import orangeschool.service.SoundContentService;
import orangeschool.service.TopicService;
import orangeschool.service.CategoryService;


@Controller    // This means that this class is a Controller
@RequestMapping(path="/util") // This
public class UtilController extends BaseController{
	@Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
	private ImageContentRepository imageContentRepository;
	@Autowired
    private ImageContentService imageContentService;
	@Autowired
    private AdminService userService;
    
	@Autowired
    private CategoryService categoryService;
	@Autowired
    private TopicService topicService;
	
	
	
    @RequestMapping(value = { "/images" }, method = RequestMethod.GET)
    public String utilImage(Model model) {
 
        model.addAttribute("message", message);
        model.addAttribute("images", imageContentRepository.findAll());
        this.menucode = "Images";
    	model.addAttribute("menucode", this.menucode);
        return "util/images";
    }
    
    
    @RequestMapping(value = { "/imageupload" }, method = RequestMethod.GET)
	public String showImageUploadPage(Model model) {
	 
	        ImageUploadForm theForm = new ImageUploadForm();
	        model.addAttribute("imageUploadForm", theForm);
	 
	        return "util/imageupload";
	}
    @RequestMapping(value = { "/imageupload" }, method = RequestMethod.POST)
    public String saveImageUploadForm(Model model, //
            @ModelAttribute("imageUploadForm") ImageUploadForm imageUploadForm) {
 
        String name = imageUploadForm.getName();
        String uri = imageUploadForm.getUri();
        String desctiprion = imageUploadForm.getDescription();
        
        
        if (this.IsValidate(name, uri)) {
        	ImageContent n = new ImageContent();
    		n.setName(name);
    		n.setUri(uri);
    		n.setDescription(desctiprion);
    		//n.setAuthorID(2);
    		imageContentRepository.save(n);
            
            return "redirect:/util/images";
        }
 
        model.addAttribute("errorMessage", errorMessage);
        return "util/imageupload";
    }
    
    
    
 // GET: Hiển thị trang form upload
    @RequestMapping(value = "/uploadOneFile", method = RequestMethod.GET)
    public String uploadOneFileHandler(Model model) {
 
        ImageUploadForm myUploadForm = new ImageUploadForm();
        model.addAttribute("imageUploadForm", myUploadForm);
 
        return "util/uploadOneFile";
    }
 
    // POST: Sử lý Upload
    @RequestMapping(value = "/uploadOneFile", method = RequestMethod.POST)
    public String uploadOneFileHandlerPOST(HttpServletRequest request, //
            Model model, //
            @ModelAttribute("imageUploadForm") ImageUploadForm myUploadForm,
            Principal _principal) {
    	User loginedUser = (User) ((Authentication) _principal).getPrincipal();
    	Admin _author = this.userService.findByUsername(loginedUser.getUsername());
        return this.doUploadImage(request, model, myUploadForm, _author);
 
    }
 
    // GET: Hiển thị trang form upload
    @RequestMapping(value = "/uploadMultiFile", method = RequestMethod.GET)
    public String uploadMultiFileHandler(Model model) {
 
       ImageUploadForm myUploadForm = new ImageUploadForm();
        model.addAttribute("imageUploadForm", myUploadForm);
 
        return "util/uploadMultiFile";
    }
 
    // POST: Sử lý Upload
    @RequestMapping(value = "/uploadMultiFile", method = RequestMethod.POST)
    public String uploadMultiFileHandlerPOST(HttpServletRequest request, //
            Model model, //
            @ModelAttribute("imageUploadForm") ImageUploadForm myUploadForm,
            Principal _principal) {
    	User loginedUser = (User) ((Authentication) _principal).getPrincipal();
    	Admin _author = this.userService.findByUsername(loginedUser.getUsername());
        return this.doUploadImage(request, model, myUploadForm, _author);
 
    }
    
    
    
    private boolean IsValidate(String _name, String _uri)
    {
    	
    	return false;
    }
    
    private String doUploadImage(HttpServletRequest request, Model model, //
            ImageUploadForm myUploadForm, Admin _author) {
 
        String description = myUploadForm.getDescription();
        System.out.println("Description: " + description);
        String name = myUploadForm.getName();
        String uri ="";
        String url ="";
        // Thư mục gốc upload file.
        String uploadRootPath = request.getServletContext().getRealPath("upload");
        System.out.println("uploadRootPath=" + uploadRootPath);
 
        File uploadRootDir = new File(uploadRootPath);
        
        // Tạo thư mục gốc upload nếu nó không tồn tại.
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
   
        }
        File imagesDir = new File(uploadRootDir,"images");
        if (!imagesDir.exists()) {
        	imagesDir.mkdirs();
        }
        String hashName = name.substring(0, 3);
        File hashDir = new File(imagesDir,hashName);
        if (!hashDir.exists()) {
        	hashDir.mkdirs();
        }
        
        
        
        MultipartFile[] fileDatas = myUploadForm.getFileDatas();
        // 
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> failedFiles = new ArrayList<String>();
        
        for (MultipartFile fileData : fileDatas) {
 
            // Tên file gốc tại Client.
            String fname = fileData.getOriginalFilename();
            int len = fname.length();
            String ext = fname.substring(len-4,len);
            System.out.println("Client File Name = " + ext);
 
            if (fname != null && fname.length() > 0) {
                try {
                    // Tạo file tại Server.
                	fname = name+ fname+ WebUtil.GetTime();
//                	MessageDigest messageDigest = MessageDigest.getInstance("MD5");
//                	messageDigest.update(fname.getBytes());
//                	String encryptedString = new String(messageDigest.digest());
                	url = "/upload/images/"+ hashName + File.separator + fname.hashCode()+ext;
                	uri = hashDir.getAbsolutePath() + File.separator + fname.hashCode()+ext;
                    File serverFile = new File(uri);
 
                    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                    stream.write(fileData.getBytes());
                    stream.close();
                    // 
                    uploadedFiles.add(serverFile);
                    System.out.println("Write file: " + serverFile);
                    
                    
                } catch (Exception e) {
                    System.out.println("Error Write file: " + name);
                    failedFiles.add(name);
                }
            }
        }
        
        
        System.out.println("Time: " + WebUtil.GetTime());
        
        
        ImageContent n = new ImageContent();
		n.setName(name);
		n.setDescription(description);
		n.setUri(uri);
		n.setUrl(url);
		n.setAuthor(_author);
		n.setCreateDate(WebUtil.GetTime());
        imageContentService.save(n);
        
        
        model.addAttribute("description", description);
        model.addAttribute("uploadedFiles", uploadedFiles);
        model.addAttribute("failedFiles", failedFiles);
        return "util/uploadResult";
    }
    
    
    
}
