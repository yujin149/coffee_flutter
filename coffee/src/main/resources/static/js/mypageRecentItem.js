//최근 본 상품 출력
function loadRecentItems(){
    const recentProducts = JSON.parse(localStorage.getItem("recentProducts")) || [];
    const container = document.getElementById("recent-products-container");
    const quickContainer = document.getElementById("quick-recent-products");

    console.log("loadRecentItems 들어왔나 확인");
    console.log("최근 본 상품 데이터:", recentProducts);

    // 마이페이지의 최근 본 상품 목록
    if (container) {
        container.innerHTML = "";
        recentProducts.forEach(product => {
            const productHtml = `
                <li class="product-item">
                    <a href="/item/${product.productId}">
                        <img src="${product.productImage}" alt="상품 이미지">
                    </a>
                </li>
            `;
            console.log("생성된 HTML:", productHtml);
            container.innerHTML += productHtml;
        });
    }

    // 퀵메뉴의 최근 본 상품 목록
    if (quickContainer) {
        quickContainer.innerHTML = "";
        recentProducts.forEach(product => {
            const productHtml = `
                <li>
                    <a href="/item/${product.productId}">
                        <img src="${product.productImage}" alt="상품 이미지">
                    </a>
                </li>
            `;
            quickContainer.innerHTML += productHtml;
        });
    }
}

//해당 페이지에서 최근본 상품 저장
function saveCookieView(productId, productImage) {
    console.log("saveCookieView 호출됨:", productId, productImage);

    let recentProducts = JSON.parse(localStorage.getItem("recentProducts")) || [];

    //기존에 있는 상품 제거
    recentProducts = recentProducts.filter(item => item.productId !== productId);
    console.log(JSON.parse(localStorage.getItem("recentProducts")));
    //새로운 상품
    recentProducts.unshift({ productId: productId, productImage: productImage });
    console.log(JSON.parse(localStorage.getItem("recentProducts")));
    console.log("추가 후 데이터:", recentProducts);
    // 최대 8개까지만 유지
    if (recentProducts.length > 8) {
        recentProducts.pop();
    }
    localStorage.setItem("recentProducts", JSON.stringify(recentProducts));
    console.log("저장된 데이터:", localStorage.getItem("recentProducts"));
}
