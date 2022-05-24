const modalOpen = (text, type, button, buttonURL) => {
  const modal = document.getElementsByClassName("modal")[0];

  modal.firstElementChild.innerHTML = text;
  modal.classList.toggle(type);

  if (button && buttonURL) {
    const buttonElement = document.createElement("a");
    buttonElement.classList = "modalbutton aux";
    buttonElement.setAttribute("onclick", buttonURL + ";return false");
    buttonElement.text = button;

    modal.insertBefore(buttonElement, modal.lastElementChild);
  }
};

const modalClose = () => {
  const modal = document.getElementsByClassName("modal")[0];

  modal.firstElementChild.innerHTML = "";
  modal.classList = "modal";

  if (document.getElementsByClassName("modalbutton aux").length != 0) {
    document.getElementsByClassName("modalbutton aux")[0].remove();
  }
};

function request(method, url, data) {
  // const apiURL = "http://10.0.0.159:4567/api";
  const apiURL = "http://localhost:4567/api";
  if (method != "GET") {
    return fetch(apiURL + url, {
      method: method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }).then((response) => response.json());
  } else {
    return fetch(apiURL + url, {
      method: method,
    })
      .then((response) => response.json())
      .catch((error) => modalOpen(error, "error"));
  }
}

const parseUpdateState = (updateState, id) => {
  switch (updateState) {
    case 0:
      return '</p><a class="passive">' + "File is in sync" + "</a>";
    case 1:
      return (
        "</p><a onclick='syncFiles(" +
        id +
        ", 1)'>" +
        "Arvuti -> Github" +
        "</a>"
      );
    case 2:
      return (
        "</p><a onclick='syncFiles(" +
        id +
        ", 2)'>" +
        "Github -> Arvuti" +
        "</a>"
      );
    case 3:
      return (
        "</p><div class='splitbutton'><a onclick='syncFiles(" +
        id +
        ", 1)'>" +
        "Arvuti -> Github" +
        "</a>" +
        "<a onclick='syncFiles(" +
        id +
        ", 2)'>" +
        "Github -> Arvuti" +
        "</a></div>"
      );
  }
};

const getAuth = () => {
  return request("GET", "/auth", "").then((a) => {
    return a;
  });
};

const submitAuth = (element) => {
  setAuthDialogButton("loading");
  const data = {
    user: element.elements["username"].value,
    pass: element.elements["password"].value,
  };

  request("POST", "/auth", data).then((response) => {
    if (response == true) {
      modalClose();
      history.go(0);
    } else {
      setAuthDialogButton("failed");
    }
  });
};

const authDialog = () => {
  getAuth().then((response) => {
    if (!response) {
      modalOpen(
        "<b>Please provide your GitHub credentials.</b> <br/> <br/> Access tokens are preferred <a>https://github.com/settings/tokens</a>",
        "login"
      );
    }
  });
};

const setAuthDialogButton = (option) => {
  const form = document.getElementsByClassName("form-box auth")[0];
  const button = document.getElementsByClassName("filebutton auth")[0];
  form.classList = "form-box auth";

  switch (option) {
    case "loading":
      form.classList.toggle("loading");
      break;
    case "failed":
      form.classList.toggle("failed");
      button.textContent = "Try again";
      break;
  }
};

const getFiles = () => {
  request("GET", "/files", "").then((a) => {
    a.map((b) => {
      addFile(b["nimi"], b["path"], b["muudatus"]);
    });
  });
};

const syncFiles = (id, locationID) => {
  const location = locationID == 1 ? "git" : "local";
  const data = { id: String(id), loc: location };

  request("POST", "/sync", data).then((a) => {
    console.log(a);
  });
};

const removeFile = (id) => {
  const row = document.getElementById(id);
  const pathAndName = row.childNodes[3].innerHTML + row.childNodes[2].innerHTML;
  const data = { path: pathAndName };

  request("DELETE", "/files", data);
};

const removeDialog = (id) => {
  const row = document.getElementById(id);
  const pathAndName = row.childNodes[3].innerHTML + row.childNodes[2].innerHTML;

  modalOpen(
    "You're about to stop tracking <b>" + pathAndName + "</b> on this device.",
    "confirm",
    "Continue",
    "removeFile(" + id + ")"
  );
};

const addDialog = (element) => {
  const path = element.elements["path"].value;
  const name = element.elements["name"].value;

  modalOpen(
    "You're about to add tracking to <b>" +
      path +
      name +
      "</b> on this device.",
    "confirm",
    "Continue",
    "addFromInput()"
  );
  return false;
};

const addFromInput = () => {
  const form = document.getElementsByClassName("fileadd")[0];
  const pathAndName = form.elements["path"].value + form.elements["name"].value;
  const data = { path: pathAndName };

  request("POST", "/files", data).then((a) => {
    console.log(a);
  });
  form.elements["path"].value = "";
  form.elements["name"].value = "";
};

const addFile = (name, path, updateState) => {
  const table = document.getElementsByClassName("filetable")[0];
  const lastId = table.lastElementChild.previousElementSibling.id;

  const id = lastId == "" ? 0 : Number(lastId) + 1;
  const action = parseUpdateState(updateState, id);
  const newRow = document.createElement("span");

  newRow.id = id;
  newRow.innerHTML =
    "<a class='filebutton remove' onclick='removeDialog(" +
    id +
    ")' title='Stop tracking this file'>-</a>" +
    '<p class="id">' +
    id +
    '</p><p class="name">' +
    name +
    '</p><p class="path">' +
    path +
    action;

  table.lastElementChild.before(newRow);
  table.lastElementChild.firstElementChild.nextElementSibling.innerHTML =
    id + 1;
};

authDialog();
getFiles();
