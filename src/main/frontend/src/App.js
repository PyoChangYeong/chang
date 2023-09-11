import './App.css';
import axios from "axios";
import {useEffect, useState} from "react";
import SocialKakao from "./login/SocialKakao";

function App() {
  const [hello, setHello] = useState('')

  useEffect(() => {
    axios.get('http://localhost:8080/hello')
        .then(response => setHello(response.data))
        .catch(error => console.log(error))
  }, []);

  return (
    <div className="App">
        <div className="kakaoLogin">
            <SocialKakao/>
        </div>
        <div className="naverLogin">

        </div>
      백엔드에서 가져온 데이터 =>  {hello}
    </div>
  );
}

export default App;
