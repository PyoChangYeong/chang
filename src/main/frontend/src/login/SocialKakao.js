import './SocialKakao.css';

const SocialKakao = ()=>
{
    const Rest_api_key='c0e752d9c4a5941d500182f4cf37f464'               //  REST API KEY
    const redirect_uri='http://localhost:3000/oauth/callback/kakao'     //  Redirect_URI
//     OAuth 요청 URL
    const kakaoURL = `https://kauth.kakao.com/oauth/authorize?client_id=${Rest_api_key}&redirect_uri=${redirect_uri}&response_type=code`
    const handleLogin=()=>{
        window.location.href = kakaoURL
    }
    return(
        <>
        <button className='StkakaoLogin' onClick={handleLogin}>카카오 로그인</button>
        </>
    )
}
export default SocialKakao