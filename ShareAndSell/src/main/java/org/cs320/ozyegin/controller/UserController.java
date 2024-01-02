package org.cs320.ozyegin.controller;

import org.cs320.ozyegin.data_layer.ImageRepository;
import org.cs320.ozyegin.data_layer.UserRepository;
import org.cs320.ozyegin.dtonutil.ImageUtil;
import org.cs320.ozyegin.model.*;
import org.cs320.ozyegin.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRepository userService;

    @Autowired
    private AdvertService advertService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BasketService basketService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("/user/sell")
    public String advertPanel(Principal p, Model m, Advertisement advertisement){
        User user = userRepository.findByEmail(p.getName());
        m.addAttribute("user", user);
        m.addAttribute("advertisement", advertisement);
        return "advertPanel";
    }

    @PostMapping("/user/sellProduct")
    public String advertPanelSell(@RequestParam("file") MultipartFile file, @ModelAttribute Advertisement advert, Principal p,Model m) throws IOException, IOException {
        User seller_user = userRepository.findByEmail(p.getName());
        advert.setSeller_id(seller_user.getId());
        advert.setSeller_name(seller_user.getName());
        if (!(Objects.equals(file.getContentType(), "image/png") || Objects.equals(file.getContentType(), "image/jpeg"))) {
            return "redirect:/user/sell?error";
        }
        advertService.saveAdvertisement(advert, file);
        return "redirect:/user/sell";
    }

    @GetMapping("/user/removeItem")
    public String removeItem(@RequestParam("basketId") Long basketId, Principal p) {
        Basket basket = basketService.findBasketById(basketId);
        basketService.deleteFromBasketByBasket(basket);
        return "redirect:/user/basket";
    }

    @PostMapping("/user/addBasket/{advertisementId}")
    public String placeOrder(@PathVariable("advertisementId") Long advertID, @RequestParam("quantity") int quantity, Principal p) throws IOException {
        User buyer = userRepository.findByEmail(p.getName());
        Advertisement advert = advertService.findAdvertByID(advertID);
        basketService.saveBasket(new Basket(), advert, quantity, buyer);
        return "redirect:/user/marketplace";
    }

    @GetMapping("/user/profile")
    public String profile(Principal p, Model m) {
        User user = userRepository.findByEmail(p.getName());
        m.addAttribute("user", user);
        Wallet wallet = walletService.findWalletByOwner_id(user);
        m.addAttribute("wallet",wallet);
        if(imageRepository.imageCheck(user.getId()) != 0) {
            Image image = imageService.findImageByOwner_id(user);
            String base64Image = java.util.Base64.getEncoder().encodeToString(image.getImageData());
            String dataUri = "data:image/jpeg;base64," + base64Image;

            m.addAttribute("image", dataUri);
        }
        return "profile";
    }


    @PostMapping("/user/profile/confirmBalance")
    public String confirmBalance(@RequestParam("addBalance") int addBalance,Principal p) {
        User user = userRepository.findByEmail(p.getName());
        Wallet wallet = walletService.findWalletByOwner_id(user);
        System.out.println("New Balance: " + addBalance);
        if (wallet != null) {
            walletService.updateBalance(wallet,addBalance);
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/user/home")
    public String home(Principal p, Model m) {
        User user = userRepository.findByEmail(p.getName());
        m.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/user/basket")
    public String basketPage(Principal p, Model model) {
        User user = userRepository.findByEmail(p.getName());
        model.addAttribute("user", user);
        List<Basket> basketList = basketService.findBasketByUser(user);
        model.addAttribute("basket", basketList);
        return "basketpage";
    }

    @PostMapping("/user/saveImage")
    public String saveImage(@RequestParam("file") MultipartFile file, Principal p) throws IOException {
        User user = userRepository.findByEmail(p.getName());
        if(imageRepository.imageCheck(user.getId()) != 0) {
            imageRepository.deleteByOwner_id(user.getId());
        }
        imageService.uploadImageForProfile(file, user.getId());
        return "redirect:/user/profile";
    }

//    @GetMapping ("/user/basket")
//    public String showBasket(Principal p, Model m) {
//        User user = userRepository.findByEmail(p.getName());
//        m.addAttribute("user", user);
//        List<Transaction> userBasket = transactionService.findBasket(user);
//        m.addAttribute("basketAds", userBasket);
//        return "basketpage";
//   }


}

//TODO: Redundancy problem can be solve by doing this ?
//private void addUserDetails(Model model, Principal p) {
//    User user = getUserByEmail(p.getName());
//    model.addAttribute("user", user);
//}
